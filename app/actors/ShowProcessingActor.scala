package actors

import akka.actor.{Actor, Props}
import akka.event.Logging
import helper._
import helper.hms.HmsUtil
import models.dto._
import models.{DownloadQueue, Show}

/**
  * Process a show, fill in information, download video, upload it and update
  * database.
  *
  * @author Matthias L. Jugel
  */

case class ScheduleNextStep(meta: ShowMetaData)

class ShowProcessingActor(backend: StorageBackend) extends Actor {

  val log = Logging(context.system, this)

  val videoDownloadActor = context.actorOf(Props[VideoDownloadActor])
  val videoUploadActor = context.actorOf(Props(new VideoUploadActor(backend)))
  val vimeoBackend = VimeoUtil.backend
  val videoVimeoUploadActor = context.actorOf(Props(new VideoUploadActor(vimeoBackend)))

  val crawlerPeriod = Config.hmsCrawlerPeriod

  def receive = {

    case meta: ShowMetaData =>
      log.info("process %s/%s: %s".format(meta.channelId, meta.stationId, meta.sourceVideoUrl))
      videoDownloadActor ! meta

    case VideoDownloadSuccess(meta) =>
      log.info("downloaded %s".format(meta.localVideoFile.getOrElse("???")))
      isVimeoUpload(meta) match {
        case true => videoVimeoUploadActor ! meta
        case false => videoUploadActor ! meta
      }

    case VideoUploadSuccess(meta) =>
      log.info("uploaded %s".format(meta.publicVideoUrl.getOrElse("???")))
      handleUploadSuccess(meta)
      self ! ScheduleNextStep(meta)

    case VideoDownloadFailure(meta, e) =>
      log.error(e, "video download failed: %s".format(meta.sourceVideoUrl.getOrElse("???")))
      handleUploadDownloadFailure(meta)
      self ! ScheduleNextStep(meta)

    case VideoUploadFailure(meta, e) =>
      log.error(e, "video upload failed: %s".format(meta.localVideoFile.getOrElse("???")))
      handleUploadDownloadFailure(meta)
      self ! ScheduleNextStep(meta)

    case ScheduleNextStep(meta) =>

      HmsUtil.isTranscoderEnabled(meta.stationId) match {

        case true => log.info(s"scheduling of next processing is not necessary for station=${meta.stationId}")

        case false =>
          log.info("schedule next processing for %s / %s in %s Min".format(meta.stationId, meta.channelId, crawlerPeriod))
          context.parent ! ScheduleProcess(ProcessStationData(meta.hmsStationId.get, meta.stationId, meta.channelId))

      }

  }

  /**
    * Tells us if we may upload to Vimeo.
    *
    * @param meta decision is based on this object
    * @return true if upload to Vimeo; false otherwise
    */
  private def isVimeoUpload(meta: ShowMetaData): Boolean = meta.vimeo.isDefined && meta.vimeo.get && meta.vimeoDone.isEmpty

  private def handleUploadDownloadFailure(meta: ShowMetaData): Unit = {
    VideoUtil.deleteLocalFile(meta)
    updateDownloadQueue(meta)
  }

  private def handleUploadSuccess(meta: ShowMetaData): Unit = {
    Show.createShowByMeta(meta)
    DownloadQueue.deleteIfExists(meta)
  }

  /**
    * Update the status of a download/upload if the HMS Transcoder is enabled.
    *
    * @param meta the base for the downloadQueue records
    * @return
    */
  private def updateDownloadQueue(meta: ShowMetaData) = {

    val station = meta.stationId

    if (HmsUtil.isTranscoderEnabled(station)) {

      log.info(s"queue download for retry: station=$station, show=${meta.showId}")
      DownloadQueue.queueDownload(meta)

    }

  }

}

