package actors

import java.io._
import javax.net.ssl.{X509TrustManager, TrustManager}

import akka.actor.Actor
import akka.actor.Status.Failure
import akka.event.Logging
import helper._

/**
 * Download the video and store it in a local file.
 *
 * @author Matthias L. Jugel
 */
class VideoDownloadActor extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case meta: ShowMetaData => try {
      val target = File.createTempFile(meta.stationId, meta.channelId)

      log.debug("target file: "+target.getPath+" "+target.getName)

      val source = meta.sourceVideoUrl match {
        case Some(u) => u
        case None => throw new FileNotFoundException("missing download URL")
      }

      val os = new FileOutputStream(target)

      Downloader.downloadFile(source, os)

      meta.localVideoFile = Some(target)

      sender() ! VideoDownloadSuccess(meta)
    } catch {
      case e: Exception =>
        log.error("downloading '%s' failed: %s".format(meta.sourceVideoUrl, e.getMessage))
        sender() ! VideoDownloadFailure(meta, e)
    }
  }
}