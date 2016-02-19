package helper


import models.Show
import models.hms.{JobResult, Source, Transcode}
import play.api.Logger
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws.{WSResponse, WS, WSRequestHolder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by dermicha on 21/06/14.
  */

case class HMSShow(ID: Int, Name: Option[String], DownloadURL: Option[String], ChannelID: Long, ParentID: Long)

object HMSShow {
  implicit val format = Json.format[HMSShow]
}

case class AccessToken(Access_Token: String)

object AccessToken {
  implicit val reads: Reads[AccessToken] = {
    (__ \ "AccessToken").read[String].map {
      l => AccessToken(l)
    }
  }
}

object HMSApi {

  //var accessToken: Option[AccessToken] = None
  //var timestamp: Long = 0

  def wsRequest(apiUrl: String) = {
    //WS.synchronized {
    WS.url(apiUrl)
      .withHeaders("x-api-version" -> "1.0")
    //}
  }

  def wsAuthRequest(apiUrl: String): Future[Option[WSRequestHolder]] = {
    HMSApi.authenticate.map {
      case Some(accessToken) =>
        Logger.debug("current Access-Token: " + accessToken.Access_Token)
        Option(wsRequest(apiUrl)
          .withHeaders("Content-Type" -> "application/json")
          .withHeaders("Access-Token" -> accessToken.Access_Token))
      case None => Logger.error("Could not get AccessToken!")
        None
    }
  }

  def authenticate: Future[Option[AccessToken]] = {
    Logger.debug("HMSApi.authenticate")

    val username = Config.hmsUserName
    val password = Config.hmsPassword
    val apiUrl = Config.hmsApiUrl + "/login/"

    Logger.debug("apiURL: " + apiUrl)
    Logger.debug("username: " + username)

    val authData = Json.obj(
      "UserName" -> JsString(username),
      "Password" -> JsString(password)
    )
    try {
      val f = wsRequest(apiUrl).post(authData)
      f.onFailure {
        case e => Logger.error("could not authenticate!", e)
          Future(None)
      }
      f.map { response =>
        response.status match {
          case s if (s < 400) && (response.body.length > 0) =>
            response.json.asOpt[AccessToken]
          case s =>
            Logger.error("no valid access token! Status: " + s)
            None
        }
      }
    } catch {
      case e: Exception =>
        Logger.error("Error while authentication", e)
        Future(None)
    }
  }

  def getShows(stationId: String, channelId: String): Future[Option[JsObject]] = {
    Logger.info("HMSApi.getShows: " + stationId)

    val encStationID = java.net.URLEncoder.encode(stationId, "UTF-8")
    val apiUrl = Config.hmsBroadcastUrl + "/Show/" + channelId + "?Category=" + encStationID + "&Order=DESC&Count=25"
    Logger.debug("HMSApi.getShows apiURL: %s".format(apiUrl))
    try {
      wsAuthRequest(apiUrl).flatMap {
        case Some(reqHolder) =>
          val f = reqHolder.get()
          f.onFailure {
            case e => Logger.error("could not fetch shows!", e)
              None
          }
          f.map { response =>
            response.status match {
              case s if s < 400 =>
                response.json \ "sources" match {
                  case errorResult: JsUndefined =>
                    Logger.error("empty result for stationId %s / channelId %s".format(stationId, channelId))
                    None
                  case result: JsValue =>
                    Some(Json.obj("shows" -> (response.json \ "sources").as[JsArray]))
                }
              case _ =>
                Logger.error("HMSApi result code: %d".format(response.status))
                None
            }
          }
        case None =>
          Logger.error("HMSApi.getCurrentShows: None")
          Future(None)
      }
    } catch {
      case e: Exception =>
        Logger.error("Error while fetching data", e)
        Future(None)
    }
  }

  def getCurrentShow(stationId: String, channelId: String): Future[Option[HMSShow]] = {
    Logger.debug("HMSApi.getCurrentShow tried for %s / %s".format(stationId, channelId))
    HMSApi.getShows(stationId, channelId).map {
      case Some(shows) =>
        Logger.info("HMSApi.getCurrentShow: shows found for %s / %s".format(stationId, channelId))
        (shows \ "shows").as[Seq[HMSShow]](Reads.seq(HMSShow.format)).find {
          aShow =>
            aShow.DownloadURL.isDefined
        }
        match {
          case Some(hmsShow) =>
            Logger.debug("found a show with download URL: %d / %s, URL: %s".format(hmsShow.ID, hmsShow.Name, hmsShow.DownloadURL))
            Some(hmsShow)
          case None =>
            Logger.error("HMSApi.getCurrentShow not successfull for %s / %s".format(stationId, channelId))
            None
        }
      case _ =>
        Logger.error("HMSApi.getCurrentShow got None as result!")
        None
    }
  }

  def transcode(show: Show): Future[Option[List[JobResult]]] = {

    val hmsBaseUrl = Config.hmsBroadcastUrl + "/hmsWSTranscode/api/transcode/"
    val channelId = java.net.URLEncoder.encode(show.channelId, "UTF-8")
    val apiUrl = s"$hmsBaseUrl/$channelId"

    Logger.debug(s"HMSApi.transcode apiURL: $apiUrl")
    try {
      wsAuthRequest(apiUrl).flatMap {

        case Some(reqHolder) =>
          callTranscode(reqHolder, show)

        case None =>
          Logger.error("HMSApi.transcode: authorization failed")
          Future(None)

      }
    } catch {
      case e: Exception =>
        Logger.error("Error while fetching data", e)
        Future(None)
    }

  }

  private def callTranscode(requestHolder: WSRequestHolder, show: Show): Future[Option[List[JobResult]]] = {

    val transcode = createTranscode(show)
    val json = Json.toJson(transcode)

    val f = requestHolder.post(json)
    f.onFailure {
      case e => Logger.error("could not call hms transcoder!", e)
        None
    }
    f.map { response =>

      response.status match {

        case s if s < 400 =>
          extractJobResults(response)

        case _ =>
          Logger.error(s"HMSApi.transcode returned error: $response")
          None

      }

    }

  }

  private def createTranscode(show: Show): Transcode = {

    val profile = Config.hmsEncodingProfile
    val destinationName: String = s"${show.showId}-${show.showSourceTitle}.mp4"
    val sources = List(Source(show.showId, None, None, None, destinationName, None, profile))

    val sourceType = "Show"
    val notificationFinished = Config.hmsEncodingNotificationFinished
    val notificationError = Config.hmsEncodingNotificationError
    val notificationStatus = Config.hmsEncodingNotificationStatus
    val callbackUrl = Config.hmsEncodingCallbackUrl
    Transcode(sourceType, sources, None, None, "HTTP", notificationFinished, notificationError, notificationStatus, callbackUrl)

  }

  private def extractJobResults(response: WSResponse): Option[List[JobResult]] = {

    response.json \ "Job" match {
      case errorResult: JsUndefined =>
        Logger.error(s"failed to parse transcode response: response=$response")
        None
      case result: JsValue =>
        Some(result.validate[List[JobResult]].get)
    }

  }

}