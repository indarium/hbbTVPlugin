package helper


import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws.{WS, WSRequestHolder}
import play.api.{Logger, Play}

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
          .withHeaders("Access-Token" -> accessToken.Access_Token))
      case None => Logger.error("Could not get AccessToken!")
        None
    }
  }

  def authenticate: Future[Option[AccessToken]] = {
    Logger.debug("HMSApi.authenticate")

    val username = Play.configuration.getString("hms.username").get
    val password = Play.configuration.getString("hms.password").get
    val apiUrl = Play.configuration.getString("hms.apiBroadcastURL").get + ("/login/")

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
    val apiUrl = Play.configuration.getString("hms.apiBroadcastURL").get + "/Show/" + channelId + "?Category=" + encStationID + "&Order=DESC&Count=25"
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
}