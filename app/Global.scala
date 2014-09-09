/**
 * >>Describe Class<<
 *
 * @author Matthias L. Jugel
 */

import actors._
import akka.actor.Props
import helper.HMSApi
import play.api._
import play.libs.Akka

object Global extends GlobalSettings {
  import play.api.Play.current

  //val showCrawler = Akka.system.actorOf(Props(new ShowCrawler()))

  override def onStart(app: Application): Unit = {
    val temp = HMSApi.authenticate

    //showCrawler ! new StartProcess
  }
}