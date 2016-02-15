package actors

import akka.actor.Actor
import akka.event.Logging
import constants.VimeoEncodingStatusSystem.DONE
import external.vimeo.VideoStatusUtil
import external.webjazz.WebjazzRest
import helper.model.ShowUtil
import helper.{Config, VimeoBackend}
import models.Show

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-01-29
  */

case class QueryVimeoVideoStatus()

class VimeoVideoStatusActor() extends Actor {

  val log = Logging(context.system, this)

  val accessToken = Config.vimeoAccessToken
  val vimeoBackend = new VimeoBackend(accessToken)

  override def receive = {

    case QueryVimeoVideoStatus =>

      Show.findShowVimeoEncodingInProgress.foreach { shows =>
        shows.foreach { show =>

          show.vimeoId match {

            case None => log.error(s"unable to query vimeo encoding status for show with missing vimeoId: showId=${show.showId}")

            case Some(vimeoId) =>

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
                val showWithSdAndHdUrl = ShowUtil.updateHdUrl(showWithSdUrl, hdFile)

                downloadSource match {

                  case Some(source) =>

                    val newShow = ShowUtil.updateEncodingStatus(showWithSdAndHdUrl, sdFile, hdFile, source)
                    Show.update(newShow)
                    log.debug(s"changed vimeoEncoding to DONE for vimeoId=$vimeoId")
                    // TODO idea: store ShowMetaData in second collection "unreleasedShow" first; move to shows collection once vimeoEncodingStatus is DONE

                    // TODO refactor Webjazz notification into separate actor
                    if (showWithSdAndHdUrl.vimeoEncodingStatus.get == DONE) {
                      val response = (new WebjazzRest).notifyWebjazz(newShow, videoStatus)
                      log.info(s"notified Webjazz: vimeoId=$vimeoId")
                      log.debug(s"notified Webjazz: vimeoId=$vimeoId, response=$response") // TODO this probably logs only the Future[] object
                    }

                  case None => log.error(s"unable to update vimeo encoding status: found no download/file with quality " +
                    s"source in vimeo response: vimeoId=$vimeoId")

                }

              }
          }
        }

      }

  }

}
