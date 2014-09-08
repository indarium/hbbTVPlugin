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
      context.system.scheduler.scheduleOnce(
        Duration.create(crawlerPeriod, TimeUnit.MINUTES),
        context.parent,
        new ProcessStation(meta.hmsStationId.get, meta.stationId, meta.channelId))

    case VideoDownloadFailure(meta, e) =>
      log.error(e, "video download failed: %s".format(meta.sourceVideoUrl.getOrElse("???")))
    // TODO consider checking the error to handle resubmission or just dropping

    case VideoUploadFailure(meta, e) =>
      log.error(e, "video upload failed: %s".format(meta.localVideoFile.getOrElse("???")))
    // TODO consider checking the error to handle resubmission or just dropping
  }
}
