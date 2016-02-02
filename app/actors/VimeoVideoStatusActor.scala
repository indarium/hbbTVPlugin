package actors

import akka.actor.Actor
import helper.VimeoBackend
import models.Show
import models.vimeo.video.{Download, Pictures}
import org.slf4j.LoggerFactory
import play.api.Play
import play.api.Play.current
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-01-29
  */
class VimeoVideoStatusActor() extends Actor {

  val log = LoggerFactory.getLogger(this.getClass)

  val accessToken = Play.configuration.getString("vimeo.accessToken").get
  val vimeoBackend = new VimeoBackend(accessToken)

  override def receive = {

    case _ =>

      val shows = Show.findShowVimeoEncodingInProgress
      for (showJson <- shows) yield {

        val show = showJson.validate[Show].get
        show.vimeoId match {

          case None => log.error(s"unable to query vimeo encoding status for show with missing vimeoId: showId=${show.vimeoId}")

          case _ =>

            for {
              videoStatusResponse <- vimeoBackend.videoStatus(show.vimeoId.get)
            } yield {

              val videoStatus = videoStatusResponse.json
              val pictures = (videoStatus \ "pictures").as[Pictures]
              val files = (videoStatus \ "files").as[Pictures]
              val downloads = (videoStatus \ "download").as[List[Download]]

              // TODO set sd url
              // TODO set hd url (if we have one)
              // TODO update vimeoEncodingStatus if necessary
              // TODO persist changes

              notifyWebjazz(show)

            }

        }

      }

  }

  private def notifyWebjazz(show: Show) = {

    val webjazzToken = Play.configuration.getString("webjazz.auth-token")
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
          webjazzResponse <- WS.url("http://mmv-mediathek.de/import/vimeo.php")
            .withHeaders(("Content-Type", "application/json"))
            .put(body)

        } yield {
          log.debug(s"Webjazz response: ${webjazzResponse.toString}")
          // TODO info log depending on webjazz https status code
        }

    }

  }

}
