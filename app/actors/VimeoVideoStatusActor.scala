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
class VimeoVideoStatusActor() extends Actor {

  val log = LoggerFactory.getLogger(this.getClass)

  val accessToken = Play.configuration.getString("vimeo.accessToken").getOrElse("NO-ACCESS-TOKEN")
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
              val pictures = VideoStatusUtil.extractPictures(videoStatus)
              val files = VideoStatusUtil.extractPictures(videoStatus )
              val downloads = VideoStatusUtil.extractPictures(videoStatus)

              // TODO set sd url
              // TODO set hd url (if we have one)
              // TODO update vimeoEncodingStatus if necessary
              // TODO persist changes

              (new WebjazzRest).notifyWebjazz(show)

            }

        }

      }

  }

}
