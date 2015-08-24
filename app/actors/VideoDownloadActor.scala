package actors

import java.io._

import akka.actor.Actor
import akka.event.Logging

import play.api.Play
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

import helper._

/**
 * Download the video and store it in a local file.
 *
 * @author Matthias L. Jugel
 */
class VideoDownloadActor extends Actor {
  val log = Logging(context.system, this)
  val minFileSize = Play.configuration.getLong("hms.minFileSize").get

  def receive = {
    case meta: ShowMetaData =>
      try {
        val target = File.createTempFile(meta.stationId, meta.channelId)

        log.debug("target file: " + target.getPath + " " + target.getName)

        val source = meta.sourceVideoUrl match {
          case Some(u) => u
          case None => throw new FileNotFoundException("missing download URL")
        }

        //val os = new FileOutputStream(target)
        //Downloader.downloadFile(source, os)
        val f = AuthDownloader.downloadFile(source.toString, target)
        f.onSuccess {
          case Some(downloadedFile: File) =>
            if (downloadedFile.length > minFileSize) {
              meta.localVideoFile = Some(target)
              sender() ! VideoDownloadSuccess(meta)
            }
            else {
              log.error(s"downloaded file size limit: ${minFileSize}")
              log.error(s"downloaded file to small: ${downloadedFile.length}")
              sender() ! VideoDownloadFailure(meta, new Exception("downloaded file to small: %d".format(target.length)))
            }
        }
      } catch {
        case t: Throwable =>
          log.error("downloading '%s' failed: %s".format(meta.sourceVideoUrl, t.getMessage))
          sender() ! VideoDownloadFailure(meta, t)
      }
  }
}
