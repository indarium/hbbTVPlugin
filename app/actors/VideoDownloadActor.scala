package actors

import java.io._

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
      val source = meta.sourceVideoUrl match {
        case Some(u) => u
        case None => throw new FileNotFoundException("missing download URL")
      }

      val os = new FileOutputStream(target)
      val is = source.openStream()
      try {
        copy(is, os)
      } finally {
        if(is != null) is.close()
        if(os != null) os.close()
      }

      meta.localVideoFile = Some(target)

      sender() ! VideoDownloadSuccess(meta)
    } catch {
      case e: Exception =>
        log.error("downloading '%s' failed: %s".format(meta.sourceVideoUrl, e.getMessage))
        sender() ! VideoDownloadFailure(meta, e)
    }
  }

  private val BUFFER_SIZE: Int = 16 * 1024
  private def copy(is: InputStream, os: OutputStream) {
    val buffer = new Array[Byte](BUFFER_SIZE)

    var count = is.read(buffer, 0, BUFFER_SIZE)
    while (count != -1) {
      os.write(buffer, 0, count)
      count = is.read(buffer, 0, BUFFER_SIZE)
    }
    os.flush()
  }
}
