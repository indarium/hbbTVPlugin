package external.webjazz

import external.vimeo.VideoStatusUtil
import external.webjazz.util.WebjazzUtil
import models.Show
import org.slf4j.LoggerFactory
import play.api.Play
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-02-03
  */
class WebjazzRest {

  val log = LoggerFactory.getLogger(this.getClass)

  def notifyWebjazz(show: Show, videoStatusJson: JsValue) = {

    val webjazzToken = Play.configuration.getString("webjazz.auth-token")
    val webjazzUrl = Play.configuration.getString("webjazz.url").getOrElse("http://mmv-mediathek.de/import/vimeo.php")

    val pictures = VideoStatusUtil.extractPictures(videoStatusJson)

    webjazzToken match {

      case None => log.error("unable to notify Webjazz: config 'webjazz.auth-token' is missing")

      case _ =>

        val auth = webjazzToken.get
        val vimeoId = show.vimeoId.get
        val hmsId = show.showId
        val width = VideoStatusUtil.extractWidth(videoStatusJson)
        val height = VideoStatusUtil.extractHeight(videoStatusJson)

        val notification = WebjazzUtil.createWebjazzNotification(auth, vimeoId, hmsId, width, height, pictures.sizes)

        log.info(s"notifying Webjazz about new video: vimeoId=$vimeoId")
        log.info(s"webjazz request: ${notification.toString}")

        for {
          webjazzResponse <- WS.url(webjazzUrl)
            .withHeaders(("Content-Type", "application/json"))
            .put(notification)

        } yield {
          log.info(s"Webjazz response: ${webjazzResponse.toString}")
          // TODO info log depending on webjazz https status code??
          // TODO return webjazzResponse??
        }

    }

  }

}
