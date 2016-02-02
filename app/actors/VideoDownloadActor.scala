package actors

import java.io._
import java.net.URL

import akka.actor.Actor
import akka.event.Logging
import helper._
import play.api.Play
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
      val currentSender = context.sender()
      try {
        val target = File.createTempFile(meta.stationId, meta.channelId)

        log.debug("target file: " + target.getPath + " " + target.getName)

        val source = meta.sourceVideoUrl match {
          case Some(u) => u
          case None => throw new FileNotFoundException("missing download URL")
        }

        val f = downloadVideo(source, target)
        f.onSuccess {
          case Some(downloadedFile: File) =>
            if (downloadedFile.length > minFileSize) {
              meta.localVideoFile = Some(target)
              currentSender ! VideoDownloadSuccess(meta)
            }
            else {
              log.error(s"downloaded file size limit: ${minFileSize}")
              log.error(s"downloaded file to small: ${downloadedFile.length}")
              currentSender ! VideoDownloadFailure(meta, new Exception("downloaded file to small: %d".format(target.length)))
            }
        }
        f.onFailure {
          case t =>
            log.error(s"download failed for: ${source.toString}")
            currentSender ! VideoDownloadFailure(meta, t)
        }
      } catch {
        case t: Throwable =>
          log.error("downloading '%s' failed: %s".format(meta.sourceVideoUrl, t.getMessage))
          currentSender ! VideoDownloadFailure(meta, t)
      }
  }

  def downloadVideo(source: URL, target: File): Future[Option[File]] = {

    Play.current.mode match {
      case play.api.Mode.Prod => AuthDownloader.downloadFile(source.toString, target)
      case _ =>
        val localPath = Play.configuration.getString("hms.localDownload").getOrElse(source.toString)
        AuthDownloader.downloadFile(localPath, target)
    }

  }

}
