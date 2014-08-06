package actors

import java.io.FileNotFoundException
import java.util.UUID

import akka.actor.Actor
import akka.event.Logging
import helper._

/**
 * Upload video to storage backend
 *
 * @author Matthias L. Jugel
 */
class VideoUploadActor(backend: StorageBackend) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case meta: ShowMetaData => try {
      val title = meta.showTitle.getOrElse(UUID.randomUUID.toString).take(1024)
      val fileName = "%s/%s/%s".format(meta.stationId, meta.channelId, title)

      val file = meta.localVideoFile match {
        case Some(f) if f.exists() => f
        case Some(f) => throw new FileNotFoundException("%s does not exist".format(f.toString))
        case None => throw new FileNotFoundException("no file")
      }
      val url = backend.store(fileName, meta.localVideoFile.get)
      file.delete()

      meta.localVideoFile = None
      meta.publicVideoUrl = Some(url)

      sender() ! VideoUploadSuccess(meta)
    } catch {
      case e: Exception =>
        log.error("upload of '%s' failed: %s".format(meta.localVideoFile.getOrElse("???"), e.getMessage))
        sender() ! VideoUploadFailure(meta, e)
    }
  }
}
