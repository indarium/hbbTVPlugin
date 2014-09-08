/**
 * >>Describe Class<<
 *
 * @author Matthias L. Jugel
 */

import actors.{ShowProcessingActor, GenericRouter, VideoDownloadActor, VideoUploadActor}
import akka.actor.Props
import helper.HMSApi
import play.api._
import play.api.libs.concurrent.Akka

object Global extends GlobalSettings {
  import play.api.Play.current

  override def onStart(app: Application): Unit = {
    val temp = HMSApi.authenticate
  }
}