package helper

import java.net.MalformedURLException

import external.vimeo.VimeoRest
import models.Show
import models.hms.TranscodeCallback
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-04-20
  */
object VideoUtil {

  private val s3 = S3Util.backend

  def deleteAllVideoRecords(showId: Long): Future[Option[Boolean]] = {

    Show.findShowById(showId) flatMap {

      case None =>
        Logger.error(s"unable to delete video: showId=$showId")
        Future(None)

      case Some(show) => deleteAllVideoRecords(show)

    }

  }

  def deleteAllVideoRecords(show: Show): Future[Option[Boolean]] = {

    val showId = show.showId

    val s3Deleted = s3Delete(show)
    for {

      vimeoDeleted <- vimeoDelete(show)

    } yield {

      s3Deleted && vimeoDeleted match {

        case true =>
          Show.delete(showId)
          TranscodeCallback.delete(showId)
          Some(true)

        case false =>
          Logger.error(s"failed to delete all video records: showId=$showId, s3Deleted=$s3Deleted, vimeoDeleted=$vimeoDeleted")
          Some(false)

      }

    }

  }

  /**
    * Delete video from Vimeo if there is one.
    *
    * @param show show on which delete is based
    * @return true if we don't have a vimeoId or the video has been deleted; false otherwise
    */
  private def vimeoDelete(show: Show): Future[Boolean] = {

    show.vimeoId match {

      case None => Future(true)
      case Some(vimeoId) => VimeoRest.videosDelete(vimeoId)

    }

  }

  private def s3Delete(show: Show): Boolean = {

    show.vimeoId match {

      case Some(vimeoId) => true

      case None =>

        try {

          val name = S3Util.extractS3FileName(show)
          Logger.info("delete")
          s3.delete(name)
          true

        } catch {

          case me: MalformedURLException =>
            Logger.error("failed to extract S3 name for show", me)
            false

          case de: DeleteException =>
            Logger.error("failed to delete video from S3", de)
            false

        }

    }

  }

}
