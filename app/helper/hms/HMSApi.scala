package helper.hms

import helper.Config
import models.Station
import play.api.Logger
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws.{WS, WSRequestHolder}

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

  def wsRequest(apiUrl: String) = {
    WS.url(apiUrl)
      .withHeaders("x-api-version" -> "1.0")
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

    Station.findStation(stationId, channelId).flatMap { station =>

        HmsUtil.getShowsUrl(station) match {

          case None =>
            Logger.debug(s"HMSApi.getShows apiURL: None ($stationId/$channelId)")
            Future(None)

          case Some(apiUrl) =>

            Logger.debug(s"HMSApi.getShows apiURL: $apiUrl")
            try {
              wsAuthRequest(apiUrl).flatMap {
                case Some(reqHolder) =>
                  val f = reqHolder.get()
                  f.onFailure {
                    case e => Logger.error(s"could not fetch shows ($stationId/$channelId)!", e)
                      None
                  }
                  f.map { response =>
                    response.status match {
                      case s if s < 400 =>
                        response.json \ "sources" match {
                          case errorResult: JsUndefined =>
                            Logger.error(s"empty result for stationId $stationId / channelId $channelId")
                            None
                          case result: JsValue =>
                            Some(Json.obj("shows" -> (response.json \ "sources").as[JsArray]))
                        }
                      case _ =>
                        Logger.error(s"HMSApi result code: ${response.status}")
                        None
                    }
                  }
                case None =>
                  Logger.error(s"HMSApi.getCurrentShows: None (stationId=$stationId/$channelId)")
                  Future(None)
              }
            } catch {
              case e: Exception =>
                Logger.error(s"Error while fetching data (stationId=$stationId/$channelId)", e)
                Future(None)
            }

        }

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