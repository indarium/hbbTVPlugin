package external.vimeo

import java.io.File
import java.util.concurrent.TimeUnit

import helper.Config
import models.dto.ShowMetaData
import play.api.Logger
import play.api.Play.current
import play.api.http.HeaderNames._
import play.api.http.MimeTypes._
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * author: cvandrei
  * since: 2016-04-11
  */
object VimeoRest {

  val vimeoApiUrl = "https://api.vimeo.com"
  val vimeoUrl = "http://vimeo.com"
  private val accessToken: String = Config.vimeoAccessToken

  def ping: Boolean = {
    val response = vimeoRequest("GET", "/", None).map(_.status == 200)
    Await.result(response, Duration(1, TimeUnit.MINUTES))
  }

  def upload(file: File): Future[Option[Long]] = {

    for {

      Some(ticket) <- uploadTicket

      uploadStatus <- uploadFile(ticket, file)
      if uploadStatus

      uploadVerified <- verifyUpload(ticket, file)
      if uploadVerified

      vimeoId <- completeUpload(ticket)

    } yield vimeoId

  }

  def uploadPostProcessing(vimeoId: Long, meta: ShowMetaData): Future[Boolean] = {

    for {

      metadataEdit <- editMetadata(vimeoId, meta)

      presetName = Config.vimeoEmbedPreset
      assignedEmbedPreset <- setEmbedPreset(vimeoId, presetName)

    //      channelAdded <- addToChannel(vimeoId, meta)

    } yield {
      metadataEdit && assignedEmbedPreset /* && channelAdded*/
    }

  }

  def videoStatus(vimeoId: Long): Future[WSResponse] = {
    Logger.debug(s"Vimeo.query: /videos/$vimeoId")
    vimeoRequest("GET", s"/videos/$vimeoId", None)
  }

  /**
    * @param id video to delete
    * @return true if video's been deleted or did not exist in the first place; false otherwise
    */
  def videosDelete(id: Long): Future[Boolean] = {

    val url = s"/videos/$id"

    vimeoRequest("DELETE", url, None) map {

      response => response.status match {

        case NO_CONTENT | NOT_FOUND =>
          Logger.info(s"deleteVideo - from Vimeo: vimeoId=$id")
          true

        case _ =>
          Logger.error(s"deleteVideo - failed for Vimeo: httpStatus=${response.status}, body=${response.body}")
          false

      }

    }

  }

  def vimeoRequest(method: String, endpoint: String, body: Option[JsObject]): Future[WSResponse] = {

    val url = vimeoApiUrl + endpoint

    val requestWithAuth = WS.url(url)
      .withHeaders((AUTHORIZATION, "bearer " + accessToken))

    val requestHolder = body match {

      case Some(aBody) =>
        requestWithAuth
          .withHeaders((CONTENT_TYPE, JSON))
          .withBody(aBody)

      case None => requestWithAuth

    }
    requestHolder.execute(method)

  }

  private def uploadTicket: Future[Option[UploadTicket]] = {

    vimeoRequest("POST", "/me/videos", Some(Json.obj("type" -> "streaming"))) map {

      response =>

        response.status match {

          case CREATED =>
            Logger.debug("successfully requested upload ticket")
            val uploadLink = (response.json \ "upload_link_secure").as[String]
            val completeUri = (response.json \ "complete_uri").as[String]
            Some(UploadTicket(uploadLink, completeUri))

          case _ =>
            Logger.error(s"failed to get upload ticket: status=${response.status}")
            None

        }

    }

  }

  private def uploadFile(ticket: UploadTicket, file: File): Future[Boolean] = {

    val requestHolder = WS.url(ticket.uploadLink)
      .withHeaders(
        CONTENT_LENGTH -> file.length.toString,
        CONTENT_TYPE -> "video/mp4"
      )
      .withRequestTimeout(600000)

    requestHolder.put(file) map {

      response => response.status match {

        case OK => true

        case _ =>
          Logger.error(s"vimeo upload failed: status=${response.status}, path=${file.getAbsolutePath}")
          false

      }

    }

  }

  private def verifyUpload(ticket: UploadTicket, file: File): Future[Boolean] = {

    val requestHolder = WS.url(ticket.uploadLink)
      .withHeaders((CONTENT_LENGTH, "0"))
      .withHeaders((CONTENT_RANGE, "bytes */*"))
    requestHolder.execute("PUT") map {

      response => response.status match {

        case 308 =>
          val uploadedBytes = response.header("Range").flatMap(_.split("-").lastOption)
          uploadedBytes.exists(_.equals(file.length.toString))

        case _ =>
          Logger.error(s"failed to verify vimeoUpload: status=${response.status}, path=${file.getAbsolutePath}")
          false

      }

    }

  }

  private def completeUpload(ticket: UploadTicket): Future[Option[Long]] = {

    vimeoRequest("DELETE", ticket.completeUri, None) map {

      response => response.status match {

        case CREATED =>

          val videoIdString = response.header("Location").flatMap(_.split("/").lastOption)
          val EmbeddedNumberFmt = """(\d+)""".r
          val videoIdLong = videoIdString.get match {

            case EmbeddedNumberFmt(n) => Some(n.toLong)
            case _ => throw new NumberFormatException(s"failed to convert id to long: vimeoId=$videoIdString")

          }
          videoIdLong

        case _ =>
          Logger.error(s"failed to complete Vimeo upload: status=${response.status}, completeUri=${ticket.completeUri}")
          None
      }


    }

  }

  private def editMetadata(vimeoId: Long, meta: ShowMetaData): Future[Boolean] = {

    val name = meta.showTitle.getOrElse("no title")
    val description = meta.showSubtitle.getOrElse("no description")

    val body = Some(
      Json.obj(
        "name" -> name,
        "description" -> description,
        "privacy" -> Json.obj(
          "view" -> "unlisted",
          "embed" -> "public",
          "download" -> false
        ),
        "review_link" -> false
      )
    )

    val path = s"/videos/$vimeoId"

    vimeoRequest("PATCH", path, body) map {

      response =>

        response.status match {

          case OK =>
            Logger.debug(s"editMetadata(): success - meta.showId=${meta.showId}, vimeoId=$vimeoId")
            true

          case _ =>
            Logger.error(s"failed to edit metadata: status=${response.status}, meta.showId=${meta.showId}, vimeoId=$vimeoId")
            false

        }

    }

  }

  private def addToChannel(vimeoId: Long, meta: ShowMetaData): Future[Boolean] = {

    val channelName = meta.channelName.getOrElse(meta.channelId)

    for {

    // check if channel exists
      getChannelsResult <- vimeoRequest("GET", "/me/channels?per_page=50&filter=moderated", None)
      if getChannelsResult.status == OK

      // get channel uri or create channel
      // TODO extract determineChannelUrl into separate method
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
      response <- vimeoRequest("PUT", channelUri + "/videos/" + vimeoId, None)

    } yield {

      response.status match {

        case NO_CONTENT => true

        case _ =>
          Logger.error(s"adding video to channel failed: status=${response.status}, vimeoId=$vimeoId")
          false
      }

    }

  }

  def setEmbedPreset(vimeoId: Long, presetName: String): Future[Boolean] = {

    getPresetId(presetName) flatMap {

      case Some(presetId) => setEmbedPreset(vimeoId, presetId)

      case None =>
        Logger.error(s"failed to determine presetId: name=$presetName")
        Future(false)

    }

  }

  def setEmbedPreset(vimeoId: Long, presetId: Long): Future[Boolean] = {

    val path = s"/videos/$vimeoId/presets/$presetId"
    vimeoRequest("PUT", path, None) map { response =>

      response.status match {

        case NO_CONTENT => true

        case _ =>
          Logger.error(s"failed to assign embedPreset: vimeoId=$vimeoId, presetId=$presetId")
          false

      }

    }

  }

  /**
    * @return response of "/me/presets"; None if request fails
    */
  def mePresets: Future[Option[JsValue]] = {

    vimeoRequest("GET", "/me/presets", None) map {

      response =>

        response.status match {

          case OK => Some(response.json)

          case _ =>
            Logger.error("failed to request presets")
            None

        }

    }

  }

  /**
    * @param presetName name of preset to which we'd like to know the id
    * @return id of given presetName; None if something goes wrong
    */
  def getPresetId(presetName: String): Future[Option[Long]] = {

    for (Some(presetResponse) <- mePresets) yield {

      val presets = (presetResponse \ "data").as[Seq[JsObject]]
      presets.find(p => (p \ "name").as[String] == presetName) match {

        case Some(preset) =>

          val uri = (preset \ "uri").as[String]
          val regex = "/users/\\d+/presets/(\\d+)".r

          try {

            uri match {
              case regex(id) => Some(id.toLong)
            }

          } catch {
            case e: MatchError =>
              Logger.error(s"failed to extract preset id from uri: uri=$uri")
              None
          }

        case None =>
          Logger.error(s"found no preset with name=$presetName")
          None

      }

    }

  }

}

case class UploadTicket(uploadLink: String, completeUri: String)
