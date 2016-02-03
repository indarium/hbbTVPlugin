package external.webjazz

import models.Show
import org.slf4j.LoggerFactory
import play.api.Play
import play.api.Play.current
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-02-03
  */
class WebjazzRest {

  val log = LoggerFactory.getLogger(this.getClass)

  def notifyWebjazz(show: Show) = {

    val webjazzToken = Play.configuration.getString("webjazz.auth-token")
    val webjazzUrl = Play.configuration.getString("webjazz.url").getOrElse("http://mmv-mediathek.de/import/vimeo.php")
    webjazzToken match {

      case None => log.error("unable to notify Webjazz: config 'webjazz.auth-token' is missing")

      case _ =>

        val vimeoId = show.vimeoId.get

        val body = Json.obj(
          "auth" -> webjazzToken.get,
          "vimeo-id" -> vimeoId,
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

        log.info(s"notifying Webjazz about new video: vimeoId=$vimeoId")
        log.debug(s"webjazz request: ${body.toString}")

        for {
          webjazzResponse <- WS.url(webjazzUrl)
            .withHeaders(("Content-Type", "application/json"))
            .put(body)

        } yield {
          log.debug(s"Webjazz response: ${webjazzResponse.toString}")
          // TODO info log depending on webjazz https status code
        }

    }

  }

}
