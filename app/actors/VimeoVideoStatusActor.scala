package actors

import akka.actor.Actor
import constants.VimeoEncodingStatusSystem.DONE
import external.vimeo.VideoStatusUtil
import external.webjazz.WebjazzRest
import helper.VimeoBackend
import helper.model.ShowUtil
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

              videoStatus = videoStatusResponse.json

              files = VideoStatusUtil.extractFiles(videoStatus)
              sdFile = VideoStatusUtil.sdFile(files)
              hdFile = VideoStatusUtil.hdFile(files)

              downloads = VideoStatusUtil.extractDownloads(videoStatus)
              downloadSource = VideoStatusUtil.downloadSource(downloads)

            } yield {

              val showWithSdUrl = ShowUtil.updateSdUrl(show, sdFile)
              val showWithSdAndHdUrl = ShowUtil.updateHdUrl(showWithSdUrl, hdFile)

              downloadSource match {

                case Some(source) =>

                  val newShow = ShowUtil.updateEncodingStatus(showWithSdAndHdUrl, sdFile, hdFile, source)

                  // TODO idea: store meta in second collection "unreleasedShow"; move to shows collection once vimeoEncodingStatus is DONE

                  // TODO persist changes -> update

                  // TODO refactor Webjazz notification into separate actor
                  if (showWithSdAndHdUrl.vimeoEncodingStatus.get == DONE) {
                    (new WebjazzRest).notifyWebjazz(newShow, videoStatus)
                  }

                case None => log.error("unable to update vimeo encoding status: found no download/file with quality source in vimeo response")

              }

            }

        }

      }

  }

}
