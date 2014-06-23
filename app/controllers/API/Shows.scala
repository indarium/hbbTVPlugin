package controllers.API

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by dermicha on 17/06/14.
 */
case class ShowApiCall(apiKey: String, stationId: String, channelId: String)

case class Show(stationId: String, channelId: String, stationName: String)

object Shows extends Controller {

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

  def current = Action(BodyParsers.parse.json) { request =>
    val showApiCall = request.body.validate[ShowApiCall]

    showApiCall.fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
      },
      showApiCall => {
        Ok(Json.obj(
          "status" -> "OK",
          "stationId" -> showApiCall.stationId,
          "stationName" -> showApiCall.stationId,
          "stationLogoUrl" -> "http://www.wiwo-wildau.de/images/neue-mitte-2010/gewerbe-logos/kw-tv.jpg",
          "stationLogoShow" -> "TRUE",
          "stationMainColor" -> "#112244",
          "channelId" -> showApiCall.channelId,
          "channelName" -> (showApiCall.channelId + " SAT"),
          "showTitle" -> "Lokal super aktuell",
          "showSubtitle" -> "Ausgabe vom 23.05.2014",
          "showLogoUrl" -> "http://images.telvi.de/images/originals/1543d38645b86053c379d529.jpg",
          "showVideoHDUrl" -> "http://cdn.mabb.indarium.de/testContent/big_buck_bunny_720p_h264.mov",
          "showVideoSDUrl" -> "http://cdn.mabb.indarium.de/testContent/big_buck_bunny_480p_h264.mov",
          "showEndInfo" -> "<h2>Sendetermine</h2><table><tr><td>Dienstag</td><td>18:00</td></tr><tr><td>Donnerstag</td><td>15:00</td></tr><tr><td>Sonntag</td><td>19:00</td></tr></table>",
          "rootPortalURL" -> "http://hbbtv.daserste.de/"
        )
        )
      }
    )
  }

}
