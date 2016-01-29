package actors

import akka.actor.Actor
import helper.VimeoBackend
import models.Show
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.Play
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.libs.ws.WS

/**
  * author: cvandrei
  * since: 2016-01-29
  */
class VimeoVideoStatusActor() extends Actor {

  val log = LoggerFactory.getLogger(this.getClass)

  val accessToken = Play.configuration.getString("vimeo.accessToken").get
  val vimeoBackend = new VimeoBackend(accessToken)

  override def receive = {

    val shows = Show.findShowVimeoEncodingInProgress

//    shows foreach { showJson =>
//
//      val show = showJson.validate[Show]
//      // ...
//
//    }

    for (showJson <- shows) {

      val show = showJson.validate[Show].get

      // query video status

      // set sd url
      // set hd url (if we have one)
      // update vimeoEncodingStatus if necessary
      //persist changes

      notifyWebjazz(show)

    }

  }

  def notifyWebjazz(show: Show) = {

    val webjazzToken = Play.configuration.getString("webjazz.auth-token")
    webjazzToken match {

      case None => log.error("unable to notify Webjazz: config 'webjazz.auth-token' is missing")

      case _ => {
        val body = Json.obj(
          "auth" -> webjazzToken.get,
          "vimeo-id" -> show.vimeoId.get,
          "hms-id" -> show.showId,
          "width" -> 1280, // TODO set value dynamically
          "height" -> 720, // TODO set value dynamically
          "thumbnails" -> JsArray(Seq(
            JsObject(Seq(
              "width" -> JsString("100"), // TODO set value dynamically
              "height" -> JsString("75"), // TODO set value dynamically
              "url" -> JsString("https://i.vimeocdn.com/video/552752804_100x75.jpg?r=pad")) // TODO set value dynamically
            ),
            JsObject(Seq(
              "width" -> JsString("1280"), // TODO set value dynamically
              "height" -> JsString("720"), // TODO set value dynamically
              "url" -> JsString("https://i.vimeocdn.com/video/552752804_1280x720.jpg?r=pad")) // TODO set value dynamically
            )
          ))
        )

        val webjazzResponse = WS.url("http://mmv-mediathek.de/import/vimeo.php")
          .withHeaders(("Content-Type", "application/json"))
          .withBody(body)
          .execute("PUT")

      }

    }

  }

}
