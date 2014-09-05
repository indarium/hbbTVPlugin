package helper

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonMappingException
import play.api.libs.functional.syntax._
import play.api.{Logger, Play}
import play.api.libs.json._
import play.api.libs.ws.{WSResponse, WSRequestHolder, WS}
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by dermicha on 21/06/14.
 */

case class HMSShow(ID: Int, Name: Option[String], DownloadURL: Option[String])

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

  def wsRequest(apiUrl: String) = {
    WS.url(apiUrl)
      .withHeaders("x-api-version" -> "1.0")
  }

  def wsAuthRequest(apiUrl: String): Future[Option[WSRequestHolder]] = {
    HMSApi.authenticate.map {
      case Some(accessToken) =>
        Logger.debug("current Access-Token: " + accessToken.Access_Token)
        Option(wsRequest(apiUrl)
          .withHeaders("Access-Token" -> accessToken.Access_Token))
      case None =>
        Logger.debug("NO current Access-Token: ")
        None
    }
  }

  def authenticate: Future[Option[AccessToken]] = {
    Logger.debug("HMSApi.authenticate")

    var username = Play.configuration.getString("hms.username").get
    val password = Play.configuration.getString("hms.password").get
    val apiUrl = Play.configuration.getString("hms.apiBaseURL").get + ("/login/")

    Logger.debug("apiURL: " + apiUrl)
    Logger.debug("username: " + username)

    val authData = Json.obj(
      "UserName" -> JsString(username),
      "Password" -> JsString(password)
    )

    wsRequest(apiUrl).post(authData).map {
      response =>
        response.status match {
          case s if s < 400 =>
            try {
              val accessToken = response.json.asOpt[AccessToken]
              Logger.debug("Access-Token: " + accessToken)
              accessToken
            } catch {
              case e: JsonMappingException =>
                Logger.error("no valid access token" + response.body, e)
                None
            }
          case _ => None
        }
    }
  }

  def getShows(stationId: String, channelId: String): Future[Option[JsObject]] = {
    Logger.debug("HMSApi.getShows: " + stationId)

    val encStationID = java.net.URLEncoder.encode(stationId, "UTF-8")
    val apiUrl = Play.configuration.getString("hms.apiBroadcastURL").get + "/Show/" + channelId + "?Category=" + encStationID + "&Order=DESC&Count=5"

    Logger.debug("encApiUrl: " + apiUrl)
    wsAuthRequest(apiUrl).flatMap {
      case Some(reqHolder) =>
        reqHolder.get().map { response =>
          response.status match {
            case s if s < 400 =>
              Logger.debug("HMSApi.getShows: result received" + response.json)
              Some(Json.obj("shows" -> (response.json \ "sources").as[JsArray]))
            case _ => Some(Json.obj("Error" -> "message"))
          }
        }
      case None => Future(None)
    }
  }

  def getCurrentShow(stationId: String, channelId: String): Future[Option[HMSShow]] = {
    HMSApi.getShows(stationId, channelId).map {
      case Some(shows) =>
        Logger.debug("shows: " + shows.toString())
        (shows \ "shows").as[Seq[HMSShow]](Reads.seq(HMSShow.format)).find {
          aShow =>
            Logger.debug(aShow.toString)
            aShow.DownloadURL.isDefined
        }
      case _ => None
    }
  }
}