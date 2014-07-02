package controllers.API

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

// Reactive Mongo imports

import reactivemongo.api._

// Reactive Mongo plugin, including the JSON-specialized collection

import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection


/**
 * Created by dermicha on 17/06/14.
 */
case class ShowApiCall(apiKey: String, stationId: String, channelId: String)

case class Show(stationId: String, channelId: String, stationName: String)

object Shows extends Controller {

  val validAPIkey = "kajsdhashdjkadh3rjhkehrkjewrhkjwrehkwerhkwhjw323hrwekjhrwer"

  val driver = new MongoDriver
  val connection = driver.connection(List("localhost"))
  val database = connection.db("hbbTVPlugin")
  val showsCollection = database.collection[JSONCollection]("shows")

  implicit val showApiCallWrites: Writes[ShowApiCall] = (
    (JsPath \ "stationId").write[String] and
      (JsPath \ "channelId").write[String] and
      (JsPath \ "stationName").write[String]

    )(unlift(ShowApiCall.unapply))

  implicit val showApiCallReads: Reads[ShowApiCall] = (
    (JsPath \ "apiKey").read[String] and
      (JsPath \ "stationId").read[String] and
      (JsPath \ "channelId").read[String]
    )(ShowApiCall.apply _)

  implicit val showWrites: Writes[Show] = (
    (JsPath \ "stationId").write[String] and
      (JsPath \ "channelId").write[String] and
      (JsPath \ "stationName").write[String]
    )(unlift(Show.unapply))

  implicit val showReads: Reads[Show] = (
    (JsPath \ "stationId").read[String] and
      (JsPath \ "channelId").read[String] and
      (JsPath \ "stationName").read[String]
    )(Show.apply _)

  def current = WithCors("POST") {

    Action.async((BodyParsers.parse.json)) { request =>
      val showApiCall = request.body.validate[ShowApiCall]

      showApiCall.map {
        showApiCall => {
          // let's do our query
          showApiCall.apiKey match {
            case validAPIkey =>
              showsCollection.
                // find all people with name `name`
                find(
                  Json.obj(
                    "stationId" -> showApiCall.stationId,
                    "channelId" -> showApiCall.channelId
                  ),
                  Json.obj("_id" -> 0)
                ).
                cursor[JsObject]
                .collect[List](1).map {
                show =>
                  show.headOption match {
                    case Some(show) => Ok(Json.obj("status" -> true) ++ show)
                    case _ => KO
                  }
              }
          }
        }
      }.getOrElse(Future.successful(KO))
    }
  }

  def KO = {
    BadRequest(Json.obj("status" -> false))
  }

  case class WithCors(httpVerbs: String*)(action: EssentialAction) extends EssentialAction with Results {
    def apply(request: RequestHeader) = {

      val origin = request.headers.get(ORIGIN).getOrElse("*")
      if (request.method == "OPTIONS") {
        // preflight
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
