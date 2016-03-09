package actors

import akka.actor.Actor
import constants.VimeoEncodingStatusSystem.DONE
import external.webjazz.WebjazzRest
import models.Show
import play.api.Logger
import play.api.libs.json.JsValue

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
        Logger.debug(s"changed vimeoEncoding to DONE for vimeoId=$vimeoId")
        val response = (new WebjazzRest).notifyWebjazz(show, videoStatus)
      }

  }

}
