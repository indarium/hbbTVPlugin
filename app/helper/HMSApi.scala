package helper


import com.fasterxml.jackson.databind.JsonMappingException
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws.{WS, WSRequestHolder}
import play.api.{Logger, Play}

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

  var accessToken: Option[AccessToken] = None
  var timestamp: Long = 0

  def wsRequest(apiUrl: String) = {
    WS.synchronized {
      WS.url(apiUrl)
        .withHeaders("x-api-version" -> "1.0")
    }
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

  def _authenticate: Future[Option[AccessToken]] = {
    Logger.debug("HMSApi.authenticate")
    Future(Some(AccessToken("5W6X0Mw+TSdkO0p+iGemA6VC/NDKc7oHuA96X0b3KocM5LKhNQAcQbargnzo0DNGIEjAjTVkaPs83ktPhCZicG6JZ2lf5R1UJ5qYpLRAUXHiJtG1IdS6tV0WslhL8Z2EgnGyS9woLqSwxU3mM/Qqx3eE5yhkU9Jw9nhbJNx/Nvovh2L3tQ8+ra5bbIv6Z7N39FrRJ06yHIOA4oSgTzTa0GIDOdEK/Ia/BasPooiDOuhfxeXgPHRhrFLpTVdwCwV4p0tDF4FtJv6i+iJTDaz6gA+2TeT4wvbN5AsoyPkepeVXikY3tXUK4pGrLgGwuLS49IU+KTodMzBEJIZ9L9lHYK9ify6+a2HlHNAodUp6VWw=")))
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

    timestamp = System.currentTimeMillis - timestamp match {
      case diff if diff > 601000L =>
        Logger.debug("*** auth timeout ***")
        accessToken = None
        System.currentTimeMillis
      case _ => timestamp
    }

    accessToken match {
      case Some(aToken) =>
        Logger.debug("cached Access-Token: " + accessToken)
        Future(Some(aToken))
      case None =>
        wsRequest(apiUrl).post(authData).map {
          response =>
            response.status match {
              case s if (s < 400) && (response.body.length > 0) =>
                try {
                  accessToken = response.json.asOpt[AccessToken]
                  Logger.debug("fresh Access-Token: " + accessToken)
                  timestamp = System.currentTimeMillis
                  accessToken
                } catch {
                  case e: JsonMappingException =>
                    Logger.error("no valid access token" + response.body, e)
                    None
                }
              case _ =>
                Logger.error("no valid access token!")
                None

            }
        }
    }
  }

  def getShows(stationId: String, channelId: String): Future[Option[JsObject]] = {
    Logger.debug("HMSApi.getShows: " + stationId)

    val encStationID = java.net.URLEncoder.encode(stationId, "UTF-8")
    val apiUrl = Play.configuration.getString("hms.apiBroadcastURL").get + "/Show/" + channelId + "?Category=" + encStationID + "&Order=DESC&Count=25"

    Logger.debug("encApiUrl: " + apiUrl)
    wsAuthRequest(apiUrl).flatMap {
      case Some(reqHolder) =>
        reqHolder.get().map { response =>
          response.status match {
            case s if s < 400 =>
              Some(Json.obj("shows" -> (response.json \ "sources").as[JsArray]))
            case _ => Some(Json.obj("Error" -> "message"))
          }
        }
      case None => Future(None)
    }
  }

  def getCurrentShow(stationId: String, channelId: String): Future[Option[HMSShow]] = {
    Logger.debug("HMSApi.getCurrentShow")
    HMSApi.getShows(stationId, channelId).map {
      case Some(shows) =>
        Logger.debug("HMSApi.getCurrentShow: shows found")
        (shows \ "shows").as[Seq[HMSShow]](Reads.seq(HMSShow.format)).find {
          aShow =>
            Logger.debug("show" + aShow.toString)
            aShow.DownloadURL.isDefined
        }
      case _ =>
        Logger.debug("HMSApi.getCurrentShow: None")
        None
    }
  }
}