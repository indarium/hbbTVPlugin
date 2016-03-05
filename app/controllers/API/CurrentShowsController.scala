package controllers.API

import java.net.URL

import actors.ShowCrawler
import akka.actor.Props
import models.dto.ProcessHmsCallback
import models.hms.TranscodeCallback
import models.{ApiKey, Show}
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
          case None => KO
        }
      case None => Future(KO)
    }
  }

  def callBack = Action(BodyParsers.parse.tolerantJson) {

    request =>
      Logger.debug("HMS CallBack-Body:")
      Logger.debug(Json.prettyPrint(request.body))

      val transcodeCallback = request.body.validate[TranscodeCallback].get
      Logger.debug(s"converted to TranscodeCallback: $transcodeCallback")

      transcodeCallback.Status match {

        case "finished" => handleEncoderFinished(transcodeCallback)
        case _ => Logger.info(s"received unfinished transcoder callback: $transcodeCallback")

      }

      // TODO update transcoCdeCallback in db
//      TranscodeCallback.save(transcodeCallback)

      Ok(Json.obj("status" -> "OK"))

  }

  private def handleEncoderFinished(transcodeCallback: TranscodeCallback) = {

    TranscodeCallback.findByHmsId(transcodeCallback.ID).map {

      dbRecord => {

        dbRecord match {

          case Some(persistedCallback) =>

            persistedCallback.meta match {

              case Some(meta) =>
                val downloadSource: String = persistedCallback.DownloadSource.get
                meta.sourceVideoUrl = Some(new URL(downloadSource))
                showCrawler ! new ProcessHmsCallback(meta)

              case None => Logger.error(s"can't process callback for which no meta exists: $transcodeCallback")

            }

          case None => Logger.error(s"failed to find db record for transcodeCallback=$transcodeCallback") // TODO respond w/ http status 500?

        }

      }

    }

  }

  private def KO = {
    BadRequest(Json.obj("status" -> false)).withHeaders(CONTENT_TYPE -> "application/json")
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
