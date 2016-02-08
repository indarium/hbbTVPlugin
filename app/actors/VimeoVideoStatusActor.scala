package actors

import akka.actor.Actor
import external.vimeo.VideoStatusUtil
import external.webjazz.WebjazzRest
import helper.VimeoBackend
import models.Show
import org.slf4j.LoggerFactory
import play.api.Play
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-01-29
  */

case class QueryVimeoVideoStatus()

class VimeoVideoStatusActor() extends Actor {

  val log = LoggerFactory.getLogger(this.getClass)

  val accessToken = Play.configuration.getString("vimeo.accessToken").getOrElse("NO-ACCESS-TOKEN")
  val vimeoBackend = new VimeoBackend(accessToken)

  override def receive = {

    case QueryVimeoVideoStatus =>

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
              val files = VideoStatusUtil.extractFiles(videoStatus)

              val sdUrl = VideoStatusUtil.sdUrl(files)
              val hdUrl = VideoStatusUtil.hdUrl(files)

              val newShow = show.copy(
                showVideoSDUrl = sdUrl.getOrElse(show.showVideoSDUrl),
                showVideoHDUrl = hdUrl
              )

              val downloads = VideoStatusUtil.extractDownloads(videoStatus)
              val downloadSource = VideoStatusUtil.downloadSource(downloads)
              /*
               * TODO update vimeoEncodingStatus if necessary; depends on:
               *   - if source HD (e.g. 1080p) then HD resolution may be at least source resolution
               *   - if source < HD then hdUrl = None
               *
               *   - sdUrl may have highest possible resolution (960x54)
               *     - unless source is less
               */

              // TODO idea: store meta in second collection "unreleased"; move to shows collection once vimeoEncodingStatus is DONE

              // TODO idea: refactor Webjazz notification into separate actor

              // TODO persist changes -> update

              (new WebjazzRest).notifyWebjazz(show, videoStatus)

            }

        }

      }

  }

}
