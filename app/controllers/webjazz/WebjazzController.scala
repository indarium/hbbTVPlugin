package controllers.webjazz

import controllers.util.ControllerUtil
import external.vimeo.VimeoRest
import models.Show
import models.hms.TranscodeCallback
import play.api.Logger
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-04-19
  */
object WebjazzController extends Controller {

  def deleteVideo(showId: Long) = Action.async {

    Logger.info(s"DELETE /api/v1/video/$showId")

    deleteAllVideoRecords(showId) map {
      case None => ControllerUtil.Unsuccessful404
      case Some(true) => ControllerUtil.statusOK
      case Some(false) => ControllerUtil.Unsuccessful400
    }

  }

  private def deleteAllVideoRecords(showId: Long): Future[Option[Boolean]] = {

    Show.findShowById(showId) flatMap {

      case None =>
        Logger.error(s"unable to delete video: showId=$showId")
        Future(None)

      case Some(show) =>

        deleteFromVimeo(show) map {

          case true =>
            val showId = show.showId
            Show.delete(showId)
            TranscodeCallback.delete(showId)
            Some(true)

          case false => Some(false)

        }

    }

  }

  /**
    * Delete video from Vimeo if there is one.
    *
    * @param show show with optional vimeoId
    * @return true if we don't have a vimeoId or the video has been deleted; false otherwise
    */
  private def deleteFromVimeo(show: Show): Future[Boolean] = {

    show.vimeoId match {
      case None => Future(true)
      case Some(vimeoId) => VimeoRest.videosDelete(vimeoId)
    }

  }

}
