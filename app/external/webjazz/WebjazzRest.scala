package external.webjazz

import external.vimeo.VideoStatusUtil
import external.webjazz.util.WebjazzUtil
import helper.Config
import models.Show
import play.api.Logger
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-02-03
  */
class WebjazzRest {

  def notifyWebjazz(show: Show, videoStatusJson: JsValue): WSResponse = {

    val notification = prepare(show, videoStatusJson)
    Logger.debug(s"about to send webjazz notification: $notification")

    execute(notification)

  }

  private def prepare(show: Show, videoStatusJson: JsValue) = {

    val webjazzToken = Config.webjazzToken
    val pictures = VideoStatusUtil.extractPictures(videoStatusJson)

    webjazzToken match {

      case "NO-ACCESS-TOKEN" => throw new IllegalArgumentException("unable to notify Webjazz: config 'webjazz.auth-token' is missing")

      case _ =>

        val auth = webjazzToken
        val vimeoId = show.vimeoId.get
        val hmsId = show.showId
        val width = VideoStatusUtil.extractWidth(videoStatusJson)
        val height = VideoStatusUtil.extractHeight(videoStatusJson)

        WebjazzUtil.createWebjazzNotification(auth, vimeoId, hmsId, width, height, pictures.sizes)

    }

  }

  private def execute(notification: JsValue): WSResponse = {

    val webjazzUrl = Config.webjazzUrl

    var result: Option[WSResponse] = None
    for {

      response <- WS.url(webjazzUrl)
        .withHeaders(("Content-Type", "application/json"))
        .put(notification)

    } yield {
      result = Some(response)
    }
    Logger.info(s"notified Webjazz: response=${result.get}")
    result.get
    // TODO handle errors by sending notifications again --> remember notification in a new collection for later sending

  }

}
