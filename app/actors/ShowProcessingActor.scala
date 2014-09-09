package actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.event.Logging
import helper._
import models.Show
import play.api.Play
import play.api.Play.current

import scala.concurrent.duration.Duration;

/**
 * Process a show, fill in information, download video, upload it and update
 * database.
 *
 * @author Matthias L. Jugel
 */

case class ScheduleNextStep(meta: ShowMetaData)

class ShowProcessingActor(backend: StorageBackend) extends Actor {

  import context._

  val log = Logging(context.system, this)

  val videoDownloadActor = context.actorOf(Props[VideoDownloadActor])
  val videoUploadActor = context.actorOf(Props(new VideoUploadActor(backend)))

  val crawlerPeriod = Play.configuration.getInt("hms.crawler.period").get

  def receive = {
    case meta: ShowMetaData =>
      log.info("process %s/%s: %s".format(meta.channelId, meta.stationId, meta.sourceVideoUrl))
      videoDownloadActor ! meta

    case VideoDownloadSuccess(meta) =>
      log.info("downloaded %s".format(meta.localVideoFile.getOrElse("???")))
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
      log.info("schedule next processing in %i Min.".format(crawlerPeriod))
      context.system.scheduler.scheduleOnce(
        Duration.create(crawlerPeriod, TimeUnit.MINUTES),
        context.parent,
        ProcessStation(meta.hmsStationId.get, meta.stationId, meta.channelId))
  }
}
