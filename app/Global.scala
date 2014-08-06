/**
 * >>Describe Class<<
 *
 * @author Matthias L. Jugel
 */

import actors.{ShowProcessingActor, GenericRouter, VideoDownloadActor, VideoUploadActor}
import akka.actor.Props
import play.api._
import play.api.libs.concurrent.Akka

object Global extends GlobalSettings {
  import play.api.Play.current

  override def onStart(app: Application) {
//    Akka.system.actorOf(Props(classOf[GenericRouter], Props[VideoDownloadActor], 2), name = "video-download")
//    Akka.system.actorOf(Props(classOf[GenericRouter], Props[VideoUploadActor], 2), name = "video-upload")
//
//    val pollActor = Akka.system.actorOf(Props[NewShowPollingActor], name = "new-show-polling")
  }
}