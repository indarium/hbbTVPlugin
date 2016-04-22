package actors

import akka.actor.Actor
import akka.event.Logging
import helper._
import models.dto.{ShowMetaData, VideoDownloadSuccess, VideoUploadFailure, VideoUploadSuccess}

/**
  * Upload video to storage backend
  *
  * @author Matthias L. Jugel
  */
class VideoUploadActor(backend: StorageBackend) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case meta: ShowMetaData => try {

      val url = backend.store(meta)

      VideoUtil.deleteLocalFile(meta)
      meta.publicVideoUrl = Some(url)

      if (meta.vimeo.isDefined && meta.vimeo.get && meta.vimeoDone.isEmpty) {
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
