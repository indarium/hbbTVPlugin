package external.webjazz

import external.vimeo.VideoStatusUtil
import external.webjazz.util.WebjazzUtil
import helper.Config
import models.Show
import org.slf4j.LoggerFactory
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

    val webjazzToken = Config.webjazzToken
    val webjazzUrl = Config.webjazzUrl

    val pictures = VideoStatusUtil.extractPictures(videoStatusJson)

    webjazzToken match {

      case "NO-ACCESS-TOKEN" => log.error("unable to notify Webjazz: config 'webjazz.auth-token' is missing")

      case _ =>

        val auth = webjazzToken
        val vimeoId = show.vimeoId.get
        val hmsId = show.showId
        val width = VideoStatusUtil.extractWidth(videoStatusJson)
        val height = VideoStatusUtil.extractHeight(videoStatusJson)

        val notification = WebjazzUtil.createWebjazzNotification(auth, vimeoId, hmsId, width, height, pictures.sizes)

        log.debug(s"notifying Webjazz about new video: vimeoId=$vimeoId")
        log.debug(s"webjazz request: ${notification.toString}")

        for {
          webjazzResponse <- WS.url(webjazzUrl)
            .withHeaders(("Content-Type", "application/json"))
            .put(notification)

        } yield {
          log.info(s"Webjazz response: ${webjazzResponse}")
          // TODO info log depending on webjazz https status code??
          // TODO return webjazzResponse??
        }

    }

  }

}
