package actors

import akka.actor.{Actor, Props}
import external.vimeo.VideoStatusUtil
import external.webjazz.WebjazzRest
import helper.VimeoUtil
import helper.model.ShowUtil
import models.Show
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-01-29
  */

case class QueryVimeoVideoStatus()

class VimeoVideoStatusActor() extends Actor {

  val vimeoBackend = VimeoUtil.backend
  val webjazzNotifyActor = context.actorOf(Props(new WebjazzNotifyActor))

  override def receive = {

    case QueryVimeoVideoStatus =>

      Show.findShowVimeoEncodingInProgress.foreach { shows =>
        shows.foreach { show =>

          show.vimeoId match {

            case None => Logger.error(s"unable to query vimeo encoding status for show with missing vimeoId: showId=${show.showId}")

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
                    webjazzNotifyActor ! WebjazzNotification(newShow, videoStatus)

                  case None => Logger.error(s"unable to update vimeo encoding status: found no download/file with quality " +
                    s"source in vimeo response: vimeoId=$vimeoId")

                }

              }
          }
        }

      }

  }

}
