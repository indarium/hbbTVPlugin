package helper

import com.fasterxml.jackson.annotation.JsonValue
import play.api.libs.functional.syntax._
import play.api.{Logger, Play}
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by dermicha on 21/06/14.
 */

case class HMSShow(hmsShowID: String, downloadUrl: String)

object HMSApi {

  implicit val showWrites: Writes[HMSShow] = (
    (JsPath \ "ID").write[String] and
      (JsPath \ "DownloadURL").write[String]
    )(unlift(HMSShow.unapply))

  implicit val showReads: Reads[HMSShow] = (
    (JsPath \ "ID").read[String] and
      (JsPath \ "DownloadURL").read[String]
    )(HMSShow.apply _)

  def wsRequest(apiUrl: String) = {
    WS.url(apiUrl)
      .withRequestTimeout(30000)
      .withHeaders("x-api-version" -> "1.0")
  }

  def wsAuthRequest(apiUrl: String) = {
    HMSApi.authenticate.map { authData =>
      wsRequest(apiUrl)
        .withHeaders("Access-Token" -> (authData \ "AccessToken").as[String])
    }
  }

  def authenticate = {
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

    wsRequest(apiUrl).post(authData)
      .map { response =>
      response.status match {
        case s if s < 300 =>
          Json.obj("AccessToken" -> (response.json \ "AccessToken").asOpt[String])
        case _ =>
          Json.obj("status" -> "Error", "message" -> "authentication failed")
      }
    }
  }

  def getShows(stationId: String, channelId: String) = {
    Logger.debug("HMSApi.getShows: " + stationId)

    val encStationID = java.net.URLEncoder.encode(stationId, "UTF-8")
    val apiUrl = Play.configuration.getString("hms.apiBroadcastURL").get + "/Show/" + channelId + "?Category=" + encStationID + "&Order=DESC&Count=1"

    Logger.debug("encApiUrl: " + apiUrl)
    wsAuthRequest(apiUrl).flatMap[JsObject] {
      requestHolder => requestHolder.get().map {
        response =>
          response.status match {
            case s if s < 400 =>
              Logger.debug("HMSApi.getShows: result received")
              //(response.json \\ "sources").reads[JsObject]
              Json.obj("result" -> response.json)
            case _ => Json.obj("status" -> "Error", "message" -> "request failed", "result" -> response.json)
          }
      }
    }
  }

  def getCurrentShow(stationId: String, channelId: String) = {
    HMSApi.getShows(stationId, channelId).map {
      shows => shows
    }
  }
}