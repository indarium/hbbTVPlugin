package actors

import akka.actor.{Actor, Props}
import akka.event.Logging
import helper._
import models.Show
import models.dto._

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
      log.info("check for vimeo exception stuff after download!!")
      if (meta.vimeo.isDefined && meta.vimeo.get && meta.vimeoDone.isEmpty)
        videoVimeoUploadActor ! meta
      else
        videoUploadActor ! meta

    case VideoUploadSuccess(meta) =>
      log.info("uploaded %s".format(meta.publicVideoUrl.getOrElse("???")))
      Show.createShowByMeta(meta)
      log.info("schedule next processing for %s / %s".format(meta.stationId, meta.channelId))
      self ! ScheduleNextStep(meta)

    case VideoDownloadFailure(meta, e) =>
      log.error(e, "video download failed: %s".format(meta.sourceVideoUrl.getOrElse("???")))
      self ! ScheduleNextStep(meta)

    case VideoUploadFailure(meta, e) =>
      log.error(e, "video upload failed: %s".format(meta.localVideoFile.getOrElse("???")))
      self ! ScheduleNextStep(meta)

    case ScheduleNextStep(meta) =>
      log.info("schedule next processing in %d Min.".format(crawlerPeriod))
      context.parent ! ScheduleProcess(ProcessStationData(meta.hmsStationId.get, meta.stationId, meta.channelId))
  }

}

