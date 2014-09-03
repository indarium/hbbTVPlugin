package actors

import java.net.URL

import akka.actor.{Props, Actor}
import akka.event.Logging
import controllers.Application._
import helper._
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Process a show, fill in information, download video, upload it and update
 * database.
 *
 * @author Matthias L. Jugel
 */
class ShowProcessingActor(backend: StorageBackend) extends Actor {
  val log = Logging(context.system, this)

  val videoDownloadActor = context.actorOf(Props[VideoDownloadActor])
  val videoUploadActor = context.actorOf(Props(new VideoUploadActor(backend)))

  def receive = {
    case meta: ShowMetaData =>
      log.info("**************************************************")
      log.info("received meta data!")
      HMSApi.getShows(meta.channelId, meta.stationId).map { shows =>
        HMSApi.getCurrentShow(meta.channelId, meta.stationId).map {
          show =>
            meta.showId = Option((show \\ "ID").toString)
            meta.sourceVideoUrl = Option(new URL((show \\ "DownloadURL").toString))
            log.info("process %s/%s: %s".format(meta.channelId, meta.stationId, meta.publicVideoUrl))
            // TODO add code to load all necessary data
            videoDownloadActor ! meta
        }
      }

    case VideoDownloadSuccess(meta) =>
      log.info("downloaded %s".format(meta.localVideoFile.getOrElse("???")))
      videoUploadActor ! meta

    case VideoUploadSuccess(meta) =>
      log.info("uploaded %s".format(meta.publicVideoUrl.getOrElse("???")))
    // TODO add code to update metadata in the database

    case VideoDownloadFailure(meta, e) =>
      log.error(e, "video download failed: %s".format(meta.sourceVideoUrl.getOrElse("???")))
    // TODO consider checking the error to handle resubmission or just dropping

    case VideoUploadFailure(meta, e) =>
      log.error(e, "video upload failed: %s".format(meta.localVideoFile.getOrElse("???")))
    // TODO consider checking the error to handle resubmission or just dropping
  }

}
