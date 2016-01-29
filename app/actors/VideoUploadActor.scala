package actors

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

      log.info("uploading file: " + meta.showTitle)

      val url = backend.store(meta)

      // delete local video file
      meta.localVideoFile.map(_.delete)
      meta.localVideoFile = None

      meta.showSourceTitle = meta.showTitle
      meta.publicVideoUrl = Some(url)

      if (meta.vimeo.isDefined && meta.vimeo.get && !meta.vimeoDone.isDefined) {
        meta.vimeoDone = Some(true)
        sender() ! VideoDownloadSuccess(meta)
      }

      sender() ! VideoUploadSuccess(meta)

    } catch {
      case e: Exception =>
        log.error("upload of '%s' failed: %s".format(meta.localVideoFile.getOrElse("???"), e.getMessage))
        sender() ! VideoUploadFailure(meta, e)
    }
  }
}
