package helper.hms


import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.JsonMappingException
import helper.Config
import models.dto.ShowMetaData
import models.hms._
import models.{Show, Station}
import play.api.Logger
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws.{WS, WSRequestHolder, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by dermicha on 21/06/14.
  */

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
    val apiUrl = Config.hmsBroadcastUrl + "/login/"

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
                      response.body.trim.isEmpty match {
                        case true =>
                          Logger.warn(s"HMSApi.getShows: empty response body ($stationId/$channelId)")
                          None
                        case false => parseSources(response, stationId, channelId)
                      }
                    case s if s == 401 =>
                      Logger.error(s"HMSApi Login failed: ${response.status}")
                      None
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

  private def parseSources(response: WSResponse, stationId: String, channelId: String): Option[JsObject] = {

    try {
      response.json \ "sources" match {
        case errorResult: JsUndefined =>
          Logger.error(s"invalid result for stationId $stationId / channelId $channelId")
          None
        case result: JsValue =>
          Some(Json.obj("shows" -> (response.json \ "sources").as[JsArray]))
      }
    } catch {
      case e: JsonMappingException =>
        Logger.error(s"HMSApi.getShows() - parsing $stationId sources failed with JsonMappingException", e)
        None
    }

  }

  def getCurrentShow(stationId: String, channelId: String): Future[Option[HmsShow]] = {

    Logger.debug("HMSApi.getCurrentShow tried for %s / %s".format(stationId, channelId))
    getAllShows(stationId, channelId).map {

      case Some(shows) =>

        val filteredShows = filterShows(stationId, shows)
        HmsUtil.extractCurrentShow(filteredShows, stationId) match {

          case Some(hmsShow) =>
            Logger.debug("HMSApi.getCurrentShow: found current show: %d / %s, URL: %s".format(hmsShow.ID, hmsShow.Name, hmsShow.DownloadURL))
            Some(hmsShow)

          case None =>

            filteredShows.isEmpty match {
              case true => Logger.info(s"nothing to do for: $stationId / $channelId")
              case false => Logger.error("HMSApi.getCurrentShow not successful for %s / %s".format(stationId, channelId))

            }
            None

        }

      case None =>
        Logger.error("HMSApi.getCurrentShow got None as result!")
        None

    }

  }

  def getAllShows(stationId: String, channelId: String): Future[Option[Seq[HmsShow]]] = {

    HMSApi.getShows(stationId, channelId).map {

      case Some(showsJson) =>
        Logger.info("HMSApi.getAllShows: shows found for %s / %s".format(stationId, channelId))
        Some((showsJson \ "shows").as[Seq[HmsShow]](Reads.seq(HmsShow.format)))

      case None => None

    }

  }

  /**
    * Creates an encoding job with HMS and returns a list of job results. The caller may create records in collection
    * "hmsTranscode" base on these job results.
    *
    * @param meta encoding job is created for this show
    * @return None if we encountered an error (errors are logged)
    */
  def transcode(meta: ShowMetaData): Future[Option[JobResult]] = {

    val apiUrl = HmsUtil.transcodeUrlPath(meta.channelId)
    try {
      wsAuthRequest(apiUrl).flatMap {

        case Some(reqHolder) => callTranscode(reqHolder, meta)

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

  def transcodeJobStatus(channelId: String, jobId: Long): Future[Option[TranscodeCallback]] = {

    val apiUrl = HmsUtil.transcodeJobUrl(channelId, jobId)
    try {
      wsAuthRequest(apiUrl) flatMap {

        case Some(reqHolder) => callJobStatusUpdate(reqHolder)

        case None =>
          Logger.error("HMS.Api.transcodeJobStatus: authorization failed")
          Future(None)

      }
    } catch {
      case e: Exception =>
        Logger.error(s"Error while querying transcode job status: channel=$channelId, ID=$jobId", e)
        Future(None)
    }

  }

  private def callTranscode(requestHolder: WSRequestHolder, meta: ShowMetaData): Future[Option[JobResult]] = {

    val transcode = createTranscode(meta)
    val json = Json.toJson(transcode)

    val f = requestHolder.post(json)
    f.onFailure {
      case e =>
        Logger.error("callTranscode() - could not call hms transcoder!", e)
        None
    }
    f.map { response =>

      response.status match {

        case s if s < 400 =>
          Logger.info(s"callTranscode() - transcoder job creation call successful: ${meta.channelId}/${meta.stationId}, show=${meta.showId}")
          Logger.debug(s", status=$s, body=${response.body}")
          extractJobResult(response)

        case _ =>
          Logger.error(s"callTranscode() - HMSApi.transcode returned error: ${meta.channelId}/${meta.stationId}, show=${meta.showId}, response=[status=${response.status}, body=${response.body}]")
          None

      }

    }

  }

  private def createTranscode(meta: ShowMetaData): Transcode = {

    val showId = meta.showId.get
    val profile = Config.hmsEncodingProfile
    val destinationName: String = s"${meta.stationId}-$showId-$profile"
    val sources = List(Source(showId, None, None, None, destinationName, None, profile))

    val sourceType = "Show"
    val notificationFinished = Config.hmsEncodingNotificationFinished
    val notificationError = Config.hmsEncodingNotificationError
    val notificationStatus = Config.hmsEncodingNotificationStatus
    val callbackUrl = Config.hmsEncodingCallbackUrl
    Transcode(sourceType, sources, None, None, "HTTP", notificationFinished, notificationError, notificationStatus, callbackUrl)

  }

  private def extractJobResult(response: WSResponse): Option[JobResult] = {

    response.json.validate[TranscodeResponse] match {

      case jsError: JsError =>
        Logger.error(s"failed to parse TranscodeResponse: ${response.body}")
        None

      case jsResult: JsResult[TranscodeResponse] => Some(jsResult.get.Jobs.head)

      case _ =>
        Logger.error(s"failed to parse transcode response: response=${response.body}")
        None

    }

  }

  private def callJobStatusUpdate(requestHolder: WSRequestHolder): Future[Option[TranscodeCallback]] = {

    val f = requestHolder.get
    f.onFailure {
      case e =>
        Logger.error("callJobStatusUpdate() failed", e)
        None
    }
    f map {
      response =>

          response.status match {

            case s if s < 400 =>
              Logger.info(s"callJobStatusUpdate() - status=$s, response=${response.body}")
              extractTranscodeCallback(response)

            case _ =>
              val url = requestHolder.url
              val status = response.status
              Logger.error(s"callJobStatusUpdate() - HMS responded with error: status=$status, url=$url")
              None

          }

    }

  }

  def extractTranscodeCallback(response: WSResponse): Option[TranscodeCallback] = {

    response.json.validate[Seq[TranscodeCallback]] match {

      case jsError: JsError =>
        Logger.error(s"failed to parse transcode job status response: ${response.body}")
        None

      case jsResult: JsResult[Seq[TranscodeCallback]] => Some(jsResult.get.head)

      case _ =>
        Logger.error(s"failed to parse transcode job status response (unknow error): ${response.body}")
        None

    }

  }

  /**
    * Filter the given show sequence if necessary.<br/>
    * Filtering can be activated with a configuration for the given stationId.<br/>
    * If filtering is activated for the given station the resulting sequence only includes shows we haven't finished or
    * began processing yet. Otherwise the original sequence is returned.
    */
  private def filterShows(stationId: String, shows: Seq[HmsShow]): Seq[HmsShow] = {

    HmsUtil.hmsImportAllShows(stationId) match {

      case false => shows
      case true => shows.filter(isShowUnknown)

    }

  }

  private def isShowUnknown(show: HmsShow): Boolean = {

    val f = for {
      existingShow <- Show.findShowById(show.ID)
      transcodeCallback <- TranscodeCallback.findByShowIdWithStatusNotFaulty(show.ID)
    } yield {
      existingShow.isEmpty && transcodeCallback.isEmpty
    }

    f onFailure {
      case e: Exception =>
        Logger.error(s"failed to find out if a show is unknown: show=$show", e)
        false
    }

    Await.result(f, Duration(30, TimeUnit.SECONDS))

  }

}