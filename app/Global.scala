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

  //

  override def onStart(app: Application): Unit = {
    // dummy call to cache the
    val temp = HMSApi.authenticate

    //inital start show crawler
    val showCrawler = Akka.system.actorOf(Props(new ShowCrawler()))
    showCrawler ! new StartProcess
  }
}