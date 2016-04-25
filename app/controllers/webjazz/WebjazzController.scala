package controllers.webjazz

import controllers.util.ControllerUtil
import helper.VideoUtil
import play.api.Logger
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-04-19
  */
object WebjazzController extends Controller {

  def deleteVideo(showId: Long) = Action.async {

    Logger.info(s"deleteVideo - /api/v1/video/$showId")

    VideoUtil.deleteAllRecords(showId) map {
      case None => ControllerUtil.Unsuccessful404
      case Some(true) => ControllerUtil.statusOK
      case Some(false) => ControllerUtil.Unsuccessful400
    }

  }

}
