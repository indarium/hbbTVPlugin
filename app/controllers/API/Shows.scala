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

  def current = Action.async((BodyParsers.parse.json)) { request =>
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
                  case Some(show) => Ok(Json.obj("status" -> "OK") ++ show)
                  case _ => KO
                }
            }
        }
      }
    }.getOrElse(Future.successful(KO))
  }

  def KO = {BadRequest(Json.obj("status" -> "KO"))}
}
