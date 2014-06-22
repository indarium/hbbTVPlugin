package helper

import play.api.{Logger, Play}
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by dermicha on 21/06/14.
 */
object HMSApi {


  def wsRequest(apiUrl: String) = {
    WS.url(apiUrl)
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

    var user = Play.configuration.getString("hms.username").get
    val password = Play.configuration.getString("hms.password").get
    val apiUrl = Play.configuration.getString("hms.apiURL").get + ("/login/")

    Logger.debug("apiURL: " + apiUrl)
    Logger.debug("username: " + user)

    val authData = Json.obj(
      "UserName" -> JsString(user),
      "Password" -> JsString(password)
    )

    wsRequest(apiUrl).post(authData)
      .map { response =>
      response.status match {
        case s if s < 300 =>
          Json.obj("AccessToken" -> (response.json \ "AccessToken"))
        case _ => Json.obj("status" -> "Error", "message" -> "authentication failed")
      }
    }
  }

  def getShows(stationID: String) = {
    Logger.debug("HMSApi.getShows: " + stationID)
    val apiUrl = Play.configuration.getString("hms.apiURL").get + "/clips/" + stationID
    Logger.debug("apiURL: " + apiUrl)
    wsAuthRequest(apiUrl).map { authRequest =>
      authRequest.get().map { response =>
        response.status match {
          case s if s < 300 =>
            Json.obj("result" -> response.json)
          case _ => Json.obj("status" -> "Error", "message" -> "authentication failed")
        }
      }
    }
  }
}


/*WS.url(apiUrl).withHeaders("x-api-version" -> "1.0")
          .withHeaders("Access-Token" -> (authResult \ "AccessToken").as[String])
          .get().map { response =>
          response.status match {
            case s if s < 300 =>
              Future[JsObject](Json.obj("result" -> response.json))
            case _ =>
              Future[JsObject](Json.obj("status" -> "getShows Error"))
          }

        } */