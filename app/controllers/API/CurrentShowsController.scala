package controllers.API

import java.net.URL

import actors.ShowCrawler
import akka.actor.Props
import constants.HmsCallbackStatus
import controllers.util.ControllerUtil
import models.hms.TranscodeCallback
import models.{ApiKey, DownloadQueue, Show}
import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.libs.Akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by dermicha on 17/06/14.
  */

case class ShowApiCall(apiKey: String, stationId: String, channelId: String)

object ShowApiCall {
  implicit val format = Json.format[ShowApiCall]
}

object CurrentShowsController extends Controller {

  val showCrawler = Akka.system.actorOf(Props(new ShowCrawler))

  //def current = WithCors("POST") {
  def current = Action.async(BodyParsers.parse.json) { request =>
    val showApiCall = request.body.as[ShowApiCall]
    Logger.debug("ShowApiCall: " + showApiCall.toString)
    ApiKey.checkApiKey(showApiCall.apiKey).flatMap {
      case Some(currentApiKey) =>
        Show.findCurrentShow(showApiCall.stationId, showApiCall.channelId).map {
          case Some(currentShowMeta) =>
            val showResult = Json.toJson(currentShowMeta).as[JsObject] ++ Json.obj("status" -> true)
            Ok(Json.prettyPrint(showResult)).withHeaders(CONTENT_TYPE -> "application/json; charset=utf-8")
          case None => ControllerUtil.KO
        }
      case None => Future(ControllerUtil.KO)
    }
  }

  def callBack = Action.async(BodyParsers.parse.tolerantJson) {

    request =>

      Logger.debug("HMS CallBack-Body:")
      Logger.debug(Json.prettyPrint(request.body))
      val callback = request.body.validate[TranscodeCallback].get
      val statusLowerCase = callback.Status.toLowerCase
      val callbackStatusLowerCase = callback.copy(Status = statusLowerCase)

      handleCallback(callbackStatusLowerCase).map {

        case true =>
          TranscodeCallback.updateRecord(callbackStatusLowerCase)
          ControllerUtil.statusOK

        case false => ControllerUtil.Unsuccessful404

      }

  }

  private def handleCallback(callback: TranscodeCallback): Future[Boolean] = {

    callback.Status match {

      case HmsCallbackStatus.FINISHED => handleEncoderFinished(callback)

      case _ =>
        Logger.info(s"received callback for unfinished transcoder job: $callback")
        Future(true)

    }

  }

  /**
    * This method takes callback with status=finished and relates to a show for which further processing is triggered
    * (download, upload to video provider, create record in collection show, etc).
    *
    * @param callback callback with status="finished"
    * @return true if everything is ok, false otherwise
    */
  private def handleEncoderFinished(callback: TranscodeCallback): Future[Boolean] = {

    TranscodeCallback.findByHmsId(callback.ID).map {

      case Some(dbRecord) => dbRecord.Status match {

        case HmsCallbackStatus.FINISHED =>
          Logger.error(s"the show this callback relates to has been processed successfully already: callback=$callback")
          false

        case _ => dbRecord.meta match {

          case Some(meta) =>
            val downloadSource = callback.DownloadSource.get
            meta.sourceVideoUrl = Some(new URL(downloadSource))
            Logger.info(s"transcoder job has finished (transcodeCallbackId=${callback.ID}). Queue download (showId=${meta.showId}).")
            DownloadQueue.queueDownload(meta)
            true

          case None =>
            Logger.error(s"ShowMetaData is missing for the callback: $callback")
            false

        }

      }

      case None =>
        Logger.error(s"received callback for unknown transcode job: jobId=${callback.ID}")
        false

    }

  }

  case class WithCors(httpVerbs: String*)(action: EssentialAction) extends EssentialAction with Results {
    def apply(request: RequestHeader) = {
      val origin = request.headers.get(ORIGIN).getOrElse("*")
      if (request.method == "OPTIONS") {
        val corsAction = Action {
          request =>
            Ok("").withHeaders(
              ACCESS_CONTROL_ALLOW_ORIGIN -> origin,
              ACCESS_CONTROL_ALLOW_METHODS -> (httpVerbs.toSet + "OPTIONS").mkString(", "),
              ACCESS_CONTROL_MAX_AGE -> "3600",
              ACCESS_CONTROL_ALLOW_HEADERS -> s"$ORIGIN, X-Requested-With, $CONTENT_TYPE, $ACCEPT, $AUTHORIZATION, X-Auth-Token",
              ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true")
        }
        corsAction(request)
      } else {
        // actual request
        action(request).map(res => res.withHeaders(
          ACCESS_CONTROL_ALLOW_ORIGIN -> origin,
          ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true"
        ))
      }
    }
  }

}
