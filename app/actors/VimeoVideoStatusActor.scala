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

      log.info("QueryVimeoVideoStatus.receive() - begin")
      val shows = Show.findShowVimeoEncodingInProgress
      for (showJson <- shows) yield {

        val show = showJson.validate[Show].get
        show.vimeoId match {

          case None => log.error(s"unable to query vimeo encoding status for show with missing vimeoId: showId=${show.showId}")

          case Some(vimeoId) =>

            log.info(s"QueryVimeoVideoStatus.receive() - processing vimeoId=$vimeoId")
            for {

              videoStatusResponse <- vimeoBackend.videoStatus(vimeoId)
              videoStatus = videoStatusResponse.json

              files = VideoStatusUtil.extractFiles(videoStatus)
              sdFile = VideoStatusUtil.sdFile(files)
              hdFile = VideoStatusUtil.hdFile(files)

              downloads = VideoStatusUtil.extractDownloads(videoStatus)
              downloadSource = VideoStatusUtil.downloadSource(downloads)

            } yield {

              val showWithSdUrl = ShowUtil.updateSdUrl(show, sdFile)
              log.info(s"updated sdUrl: ${showWithSdUrl.showVideoSDUrl}")
              val showWithSdAndHdUrl = ShowUtil.updateHdUrl(showWithSdUrl, hdFile)
              log.info(s"updated hdUrl: ${showWithSdAndHdUrl.showVideoHDUrl}")

              downloadSource match {

                case Some(source) =>

                  val newShow = ShowUtil.updateEncodingStatus(showWithSdAndHdUrl, sdFile, hdFile, source)
                  Show.update(newShow)
                  log.info(s"current vimeoEncodingStatus: ${newShow.vimeoEncodingStatus}")
                  // TODO idea: store ShowMetaData in second collection "unreleasedShow" first; move to shows collection once vimeoEncodingStatus is DONE

                  // TODO refactor Webjazz notification into separate actor
                  if (showWithSdAndHdUrl.vimeoEncodingStatus.get == DONE) {
                    (new WebjazzRest).notifyWebjazz(newShow, videoStatus)
                  }

                case None => log.error(s"unable to update vimeo encoding status: found no download/file with quality " +
                  s"source in vimeo response: vimeoId=$vimeoId")

              }

            }

        }

      }
      log.info("QueryVimeoVideoStatus.receive() - end")

  }

}
