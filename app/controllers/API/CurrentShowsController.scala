package controllers.API

import models.{ApiKey, Show}
import play.api._
import play.api.libs.json._
import play.api.mvc._

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

  def current = WithCors("POST") {
    Action.async((BodyParsers.parse.json)) { request =>
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
  }

  def KO = {
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
