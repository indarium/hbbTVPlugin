package helper

import java.io.File
import java.net.URL
import java.util.UUID

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import constants.VimeoEncodingStatusSystem._
import external.vimeo.VimeoRest
import models.dto.ShowMetaData
import play.api.Logger

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

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
    Logger.info(s"delete from S3: $name")
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

            Some(vimeoId) <- VimeoRest.upload(file)
            modifyVideo <- VimeoRest.uploadPostProcessing(vimeoId, meta)
            if modifyVideo

          } yield {

            meta.vimeoId = Some(vimeoId)
            meta.vimeoEncodingStatus = Some(IN_PROGRESS)
            Logger.info(s"Finished upload to Vimeo: ${meta.stationId} / ${meta.showTitle} / ${meta.showId.get} / ${meta.vimeoId}")

            // TODO ??use url from /videos/${VIDEO-ID} response instead??
            // TODO ??field showVideoSDUrl might as well be optional??
            new URL(vimeoUrl + "/" + vimeoId)

          }

          Await.result(res, 10 minute)

        } catch {
          case e: Exception => throw new StorageException(s"Could not upload to Vimeo: file=${file.getAbsolutePath}", e)
        }
    }
  }

  /**
    * @param vimeoId vimeoId of the video
    */
  override def delete(vimeoId: String): Unit = {
    try {
      VimeoRest.videosDelete(vimeoId.toLong)
    } catch {
      case e: Exception => throw new DeleteException("can't delete %s".format(vimeoId), e)
    }
  }

  override def retrieve(name: String, file: Option[File]): File = ??? // not needed

  override def list(): List[String] = ???

}
