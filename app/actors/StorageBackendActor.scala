package actors

import akka.actor.Actor
import akka.event.Logging
import helper.{StorageBackend, StorageMedia}

/**
 *
 *
 * @author Matthias L. Jugel
 */
class StorageBackendActor(backend: StorageBackend) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case ("store", media: StorageMedia) =>
      try {
        sender() ! backend.store(media)
      } catch {
        case e: Exception =>
          log.error(e, "upload failed")
          sender() ! akka.actor.Status.Failure(e)
      }
    case ("retrieve", media: StorageMedia) =>
      sender() ! backend.retrieve(media)
    case ("delete", media: StorageMedia) =>
      backend.delete(media)
    case _ =>
      log.error("received unknown message")
  }

}
