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

case class HMSShow(ID: Long, Name: Option[String], DownloadURL: Option[String], ChannelID:Long, ParentID:Long)

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
      case None =>
        Logger.error("Could not get AccessToken!")
        None
    }
  }

  def authenticate: Future[Option[AccessToken]] = {
    Logger.debug("HMSApi.authenticate")

    var username = Play.configuration.getString("hms.username").get
    val password = Play.configuration.getString("hms.password").get
    val apiUrl = Play.configuration.getString("hms.apiBroadcastURL").get + ("/login/")

    Logger.debug("apiURL: " + apiUrl)
    Logger.debug("username: " + username)

    val authData = Json.obj(
      "UserName" -> JsString(username),
      "Password" -> JsString(password)
    )

    //    timestamp = System.currentTimeMillis - timestamp match {
    //      case diff if diff > 601000L =>
    //        Logger.debug("*** auth timeout ***")
    //        accessToken = None
    //        System.currentTimeMillis
    //      case _ => timestamp
    //    }

    //    accessToken match {
    //      case Some(aToken) =>
    //        Logger.debug("cached Access-Token: " + accessToken)
    //        Future(Some(aToken))
    //      case None =>
    try {
      wsRequest(apiUrl).post(authData).map {
        response =>
          response.status match {
            case s if (s < 400) && (response.body.length > 0) =>
              val accessToken = response.json.asOpt[AccessToken]
              Logger.debug("fresh Access-Token: " + accessToken)
              //accessToken = response.json.asOpt[AccessToken]
              //timestamp = System.currentTimeMillis
              accessToken
            case _ =>
              Logger.error("no valid access token!")
              None

          }
      }
    } catch {
      case e: Exception =>
        Logger.error("Error while try to authenticate", e)
        Future(None)
      case _: Throwable =>
        Logger.error("unknown error while try to authenticate")
        Future(None)
    }
    //}
  }

  def getShows(stationId: String, channelId: String): Future[Option[JsObject]] = {
    Logger.debug("HMSApi.getShows: " + stationId)

    val encStationID = java.net.URLEncoder.encode(stationId, "UTF-8")
    val apiUrl = Play.configuration.getString("hms.apiBroadcastURL").get + "/Show/" + channelId + "?Category=" + encStationID + "&Order=DESC&Count=25"

    try {
      wsAuthRequest(apiUrl).flatMap {
        case Some(reqHolder) =>
          reqHolder.get().map { response =>
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
          case None => None
        }
      case _ =>
        Logger.error("HMSApi.getCurrentShow got None as result!")
        None
    }
  }
}