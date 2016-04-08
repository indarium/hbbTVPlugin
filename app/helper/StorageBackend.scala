package helper

import java.io.File
import java.net.URL
import java.util.UUID

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import constants.VimeoEncodingStatusSystem._
import models.dto.ShowMetaData
import play.api.Logger
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws.{WS, WSResponse}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Generic storage backend trait defining the functionality for a backend.
  */
trait StorageBackend {
  /**
    * Stora a media file in the backend.
    *
    * @param meta the show meta data. Must contain the video file
    * @return the url to the stored file, if successful
    */
  def store(meta: ShowMetaData): URL

  /**
    * Retrieve a media file from the backend.
    *
    * @param name the file name on the backend
    * @return the media file as a local file
    */
  def retrieve(name: String, file: Option[File] = None): File

  /**
    * Delete a media from the backend.
    *
    * @param name the name of the file on the backend
    * @return true for successful deletion
    */
  def delete(name: String)

  /**
    * List backend storage contents.
    *
    * @return a list of media file names on the backend
    */
  def list(): List[String]
}

// Storage exceptions (wrap original exception)
class VideoFileNotFindException(m: String) extends Exception(m)

class StorageException(m: String, t: Throwable) extends Exception(m, t)

class RetrieveException(m: String, t: Throwable) extends Exception(m, t)

class DeleteException(m: String, t: Throwable) extends Exception(m, t)

class VimeoRequestException(m: String) extends Exception(m)

/**
  * A S3 storage backend that stores the media files on S3 using the path returned by StorageMedia.toString
  * as the key. This backend requires credentials and a bucket name to work.
  *
  * @param credentials the S3 credentials to use
  * @param bucket      the bucket name
  */
class S3Backend(credentials: AWSCredentials, bucket: String) extends StorageBackend {
  private val s3 = new AmazonS3Client(credentials)

  override def store(meta: ShowMetaData) = try {
    meta.localVideoFile match {
      case None => throw new VideoFileNotFindException("No video file defined.")
      case Some(file) if !file.isFile => throw new VideoFileNotFindException("File does not exist: " + meta.localVideoFile.get)

      case Some(file) =>

        val fileName = "%s/%s/%s.%s".format(meta.stationId, meta.channelId, UUID.randomUUID.toString.take(64), "mp4")
        Logger.info(s"upload to S3: ${meta.stationId} / ${meta.showTitle} / ${meta.showId.get} / $fileName")
        s3.putObject(bucket, fileName, file)

        val s3Url = s3.getUrl(bucket, fileName)
        Logger.info(s"Finished upload to S3: ${meta.stationId} / ${meta.showTitle} / ${meta.showId.get} / ${s3Url.getPath}")
        // create cdn url
        new URL(Config.cdnBaseUrl + s3Url.getPath)

    }
  } catch {
    case e: Exception => throw new StorageException("can't store %s".format(meta.showTitle), e)
  }

  override def retrieve(name: String, file: Option[File] = None): File = try {
    val localFile = file match {
      case Some(file: File) if file.exists && file.canWrite => file
      case None => File.createTempFile(UUID.randomUUID().toString, ".tmp")
    }
    val getObjectRequest = new GetObjectRequest(bucket, name)
    s3.getObject(getObjectRequest, localFile)
    localFile
  } catch {
    case e: Exception => throw new RetrieveException("can't retrieve %s".format(name), e)
  }

  override def delete(name: String) = try {
    s3.deleteObject(bucket, name)
  } catch {
    case e: Exception => throw new DeleteException("can't delete %s".format(name), e)
  }

  override def list() = try {
    s3.listObjects(bucket).getObjectSummaries.asScala.map(_.getKey).toList
  } catch {
    case e: Exception => throw new StorageException("can't retrieve file list", e)
  }
}

/**
  * Vimeo storage backend. It uploads and manages videos on vimeo. The key (name) is the videoId which is assigned by vimeo.
  * An accessToken with write permissions is required
  *
  * @param accessToken the vimeo API key to be used
  */
class VimeoBackend(accessToken: String) extends StorageBackend {

  val vimeoApiUrl = "https://api.vimeo.com"
  val vimeoUrl = "http://vimeo.com"

  override def store(meta: ShowMetaData): URL = {

    meta.localVideoFile match {
      case None => throw new VideoFileNotFindException("No video file defined.")
      case Some(file) if !file.isFile => throw new VideoFileNotFindException("File does not exist: " + meta.localVideoFile.get)
      case Some(file) =>
        try {
          Logger.info(s"upload to Vimeo: ${meta.stationId} / ${meta.showTitle} / ${meta.showId.get} / ${file.getAbsolutePath}")

          val res = for {
          // request upload ticket
            ticketResponse <- vimeoRequest("POST", "/me/videos", Some(Json.obj("type" -> "streaming")))
            if ticketResponse.status == 201

            // get the data we need from the ticketResponse
            uploadLinkSecure = (ticketResponse.json \ "upload_link_secure").as[String]
            completeUri = (ticketResponse.json \ "complete_uri").as[String]

            // upload file
            uploadResponse <- vimeoUploadFile(uploadLinkSecure, file)
            if uploadResponse.status == 200

            // verify upload
            verifyResponse <- vimeoVerifyUpload(uploadLinkSecure, file)
            if verifyResponse.status == 308

            // get the number of bytes that vimeo received and check if they correspond to the file size
            uploadedBytes = verifyResponse.header("Range").flatMap(_.split("-").lastOption)
            if uploadedBytes.exists(_.equals(file.length.toString))

            // close upload ticket and mark upload complete
            finishResponse <- vimeoRequest("DELETE", completeUri, None)
            if finishResponse.status == 201

            // get video id from response
            videoId = finishResponse.header("Location").flatMap(_.split("/").lastOption)
            if videoId.isDefined

            // update metadata
            metadataResponse <- {
              editMetaData(videoId.get, meta)
            }
            if metadataResponse.status == 200

            // add video to channel
            addChannelResponse <- addToChannel(videoId.get, meta)
            if addChannelResponse.status == 204

            assignEmbedPresetResponse <- assignEmbedPreset(videoId.get)
            if assignEmbedPresetResponse.status == 204

          } yield {

            val EmbeddedNumberFmt = """(\d+)""".r
            val videoIdLong = videoId.get match {
              case EmbeddedNumberFmt(n) => Some(n.toLong)
              case _ => throw new NumberFormatException(s"failed to convert id to long: vimeoId=$videoId")
            }
            Logger.debug(s"videoId=$videoId; vimeoId=$videoIdLong")

            meta.vimeoId = videoIdLong
            meta.vimeoEncodingStatus = Some(IN_PROGRESS)

            Logger.info(s"Finished upload to Vimeo: ${meta.stationId} / ${meta.showTitle} / ${meta.showId.get} / ${videoIdLong}")

            // TODO ??use url from /videos/${VIDEO-ID} response instead??
            // TODO ??field showVideoSDUrl might as well be optional??
            new URL(vimeoUrl + "/" + videoId.get)

          }

          Await.result(res, 10 minute)

        } catch {
          case e: Exception => throw new StorageException("Could not upload to Vimeo", e)
        }
    }
  }

  override def delete(name: String): Unit = {
    try {
      val url = vimeoApiUrl + "/videos/" + name
      vimeoRequest("DELETE", url, None)
    } catch {
      case e: Exception => throw new DeleteException("can't delete %s".format(name), e)
    }
  }

  override def retrieve(name: String, file: Option[File]): File = ??? // not needed

  override def list(): List[String] = ???

  def ping: Boolean = {
    val res = vimeoRequest("GET", "/", None).map(_.status == 200)
    Await.result(res, 1.minute)
  }

  def editMetaData(videoId: String, meta: ShowMetaData): Future[WSResponse] = {

    //    Logger.debug("Vimeo: adding metadata to video. name: " + meta.showTitle + " description: " + meta.showSubtitle)

    val name = meta.showTitle.getOrElse("no title")
    val description = meta.showSubtitle.getOrElse("no description")

    val body = Json.obj(
      "name" -> name,
      "description" -> description,
      "privacy.view" -> "unlisted",
      "privacy.embed" -> "public",
      "privacy.download" -> false,
      "review_link" -> "false"
    )
    val path = "/videos/" + videoId

    vimeoRequest("PATCH", path, Some(body))
  }

  def addToChannel(videoId: String, meta: ShowMetaData): Future[WSResponse] = {

    //    Logger.debug("Vimeo: adding video to channel: " + meta.channelName )

    val channelName = meta.channelName.getOrElse(meta.channelId)

    for {
    // check if channel exists
      getChannelsResult <- vimeoRequest("GET", "/me/channels?per_page=50&filter=moderated", None)
      if getChannelsResult.status == 200

      // get channel uri or create channel
      channelUri <- {
        val channels = (getChannelsResult.json \ "data").as[Seq[JsObject]]
        channels.find(c => (c \ "name").as[String].equals(channelName)) match {
          case Some(channel) => Future((channel \ "uri").as[String])
          case None => vimeoRequest("POST", "/channels", Some(Json.obj("name" -> channelName))).map {
            response => (response.json \ "uri").as[String]
          }
        }
      }

      // add video to channel
      addChannelResponse <- vimeoRequest("PUT", channelUri + "/videos/" + videoId, None)

    } yield {
      addChannelResponse
    }
  }

  def assignEmbedPreset(videoId: String): Future[WSResponse] = {

    val embedPreset = Config.vimeoEmbedPreset
    val path = s"/videos/$videoId/presets/$embedPreset"

    Logger.debug(s"Vimeo.query: $path")
    vimeoRequest("PUT", path, None)

  }

  def videoStatus(vimeoId: Long): Future[WSResponse] = {
    Logger.debug(s"Vimeo.query: /videos/$vimeoId")
    vimeoRequest("GET", s"/videos/$vimeoId", None)
  }

  def vimeoRequest(method: String, endpoint: String, body: Option[JsObject]): Future[WSResponse] = {

    val url = vimeoApiUrl + endpoint

    val wsRequestHolder = WS.url(url)
      .withHeaders(("Authorization", "bearer " + accessToken))

    body match {
      case Some(aBody) =>
        wsRequestHolder
          .withHeaders(("Content-Type", "application/json"))
          .withBody(aBody)
          .execute(method)
      case None =>
        wsRequestHolder
          .execute(method)
    }

  }

  def vimeoUploadFile(uploadLink: String, file: File): Future[WSResponse] = {
    WS.url(uploadLink)
      .withHeaders(("Content-Length", file.length.toString))
      .withHeaders(("Content-Type", "video/mp4"))
      .withRequestTimeout(600000)
      .put(file)
  }

  def vimeoVerifyUpload(uploadLink: String, file: File): Future[WSResponse] = {
    WS.url(uploadLink)
      .withHeaders(("Content-Length", "0"))
      .withHeaders(("Content-Range", "bytes */*"))
      .execute("PUT")
  }

}
