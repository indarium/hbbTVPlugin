package actors

import java.io.FileNotFoundException
import java.net.URL
import java.util.UUID

import akka.actor.Actor
import akka.event.Logging
import helper._
import play.api.Play
import play.api.Play.current

/**
 * Upload video to storage backend
 *
 * @author Matthias L. Jugel
 */
class VideoUploadActor(backend: StorageBackend) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case meta: ShowMetaData => try {

      log.info("uploading file: " + meta.showTitle)

      val url = backend.store(meta)

      // delete local video file, if it exists
      meta.localVideoFile.map(_.delete)

      meta.localVideoFile = None
      meta.showSourceTitle = meta.showTitle
      meta.publicVideoUrl = Some(url)

      sender() ! VideoUploadSuccess(meta)

    } catch {
      case e: Exception =>
        log.error("upload of '%s' failed: %s".format(meta.localVideoFile.getOrElse("???"), e.getMessage))
        sender() ! VideoUploadFailure(meta, e)
    }
  }
}
