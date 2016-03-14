package actors

import akka.actor.Actor
import constants.VimeoEncodingStatusSystem.{DONE, IN_PROGRESS}
import external.webjazz.WebjazzRest
import external.webjazz.util.WebjazzUtil
import models.Show
import play.api.Logger
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by cvandrei on 2016-03-09.
  */
case class WebjazzNotification(show: Show,
                               videoStatus: JsValue
                              )

class WebjazzNotifyActor extends Actor {

  override def receive = {

    case WebjazzNotification(show, videoStatus) =>

      val vimeoId: Long = show.vimeoId.get

      if (show.vimeoEncodingStatus.get == DONE) {

        WebjazzUtil.isNotificationEnabled(show.stationId) match {

          case false => Logger.debug(s"webjazz notifications are disabled for stationId=${show.stationId}")

          case true =>

            Logger.debug(s"vimeoEncoding is DONE for vimeoId=$vimeoId; notify Webjazz next")
            (new WebjazzRest).notifyWebjazz(show, videoStatus).map {

              case true => Logger.info(s"notified Webjazz: showId=${show.showId}, vimeoId=${show.vimeoId}")

              case false =>
                Logger.error(s"failed to notify Webjazz: showId=${show.showId}, vimeoId=${show.vimeoId}")
                val showInProgress = show.copy(vimeoEncodingStatus = Some(IN_PROGRESS))
                Show.update(showInProgress)

            }

        }

      }

  }

}
