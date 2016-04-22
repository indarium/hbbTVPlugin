package helper

import java.net.MalformedURLException

import external.vimeo.VimeoRest
import models.Show
import models.dto.ShowMetaData
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

  /**
    * Delete a show from every video provider and all related database records.
    *
    * @param showId id of the show we'd like to delete
    * @return None if show does not exist; true if deletion was successful; false if if deletion failed
    */
  def deleteAllRecords(showId: Long): Future[Option[Boolean]] = {

    Show.findShowById(showId) flatMap {

      case None =>
        Logger.error(s"unable to delete video: showId=$showId")
        Future(None)

      case Some(show) =>
        deleteAllRecords(show) map {
          result => Some(result)
        }

    }

  }

  /**
    * Delete a show from every video provider and all related database records.
    *
    * @param show the show we'd like to delete
    * @return true if deletion was successful; false if if deletion failed
    */
  def deleteAllRecords(show: Show): Future[Boolean] = {

    val showId = show.showId
    Logger.debug(s"deleteVideo - attempt to delete showId=$showId")

    val s3Deleted = s3Delete(show)
    for (vimeoDeleted <- vimeoDelete(show)) yield {

      s3Deleted && vimeoDeleted match {

        case true =>
          Show.delete(showId)
          TranscodeCallback.delete(showId)
          true

        case false =>
          Logger.error(s"deleteVideo - failed to delete all records: showId=$showId, s3Deleted=$s3Deleted, vimeoDeleted=$vimeoDeleted")
          false

      }

    }

  }

  def deleteLocalFile(meta: ShowMetaData) = {
    meta.localVideoFile.map(_.delete)
    meta.localVideoFile = None
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

    try {

      val name = S3Util.extractS3FileName(show)
      s3.delete(name)
      Logger.info(s"deleteVideo - from S3: name=$name")
      true

    } catch {

      case me: MalformedURLException =>
        Logger.error("deleteVideo - failed to extract S3 name for show", me)
        false

      case de: DeleteException =>
        Logger.error(s"deleteVideo - failed for S3", de)
        false

    }

  }

}
