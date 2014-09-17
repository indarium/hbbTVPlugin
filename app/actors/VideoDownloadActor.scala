package actors

import java.io._

import akka.actor.Actor
import akka.event.Logging
import helper._
import play.api.Play
import play.api.Play.current

/**
 * Download the video and store it in a local file.
 *
 * @author Matthias L. Jugel
 */
class VideoDownloadActor extends Actor {
  val log = Logging(context.system, this)
  val minFileSize = Play.configuration.getInt("hms.minFileSize").get

  def receive = {
    case meta: ShowMetaData => try {
      val target = File.createTempFile(meta.stationId, meta.channelId)

      log.debug("target file: " + target.getPath + " " + target.getName)

      val source = meta.sourceVideoUrl match {
        case Some(u) => u
        case None => throw new FileNotFoundException("missing download URL")
      }

      val os = new FileOutputStream(target)

      Downloader.downloadFile(source, os)
      if (target.length() > minFileSize) {
        meta.localVideoFile = Some(target)
        sender() ! VideoDownloadSuccess(meta)
      }
      else {
        log.error("downloaded file to small: %d".format(target.getTotalSpace))
        sender() ! VideoDownloadFailure(meta, new Exception("downloaded file to small: %d".format(target.getTotalSpace)))
      }

    } catch {
      case e: Exception =>
        log.error("downloading '%s' failed: %s".format(meta.sourceVideoUrl, e.getMessage))
        sender() ! VideoDownloadFailure(meta, e)
    }
  }
}
