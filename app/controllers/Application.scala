package controllers

import actors.{ShowCrawler, StartProcess}
import akka.actor.Props
import akka.util.Timeout
import helper._
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSRequestHolder}
import play.api.mvc._
import play.libs.Akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object Application extends Controller {

  val showCrawler = Akka.system.actorOf(Props(new ShowCrawler()))

  def index = Action {
    implicit val timeout = Timeout(5 seconds)

    //    val s3file = for {
    //      x <- ask(s3Uploader, UploadJob("",File.createTempFile("s3.",".tmp"),"","")).mapTo[S3File]
    //    } yield(x)

    Ok("HbbTV Plugin")

    /*HMSApi.authenticate.map { resultData =>
      Ok(resultData.toString)
    }*/

    /*HMSApi.getShows("OSF").map { resultData =>
      Ok("hallo" + resultData.toString)
    }*/
  }

  def testJson = Action.async {
    val url = "http://ip.jsontest.com/"

    val holder: WSRequestHolder = WS.url(url)

    holder
      .get()
      .map {
      response =>
        Ok("ip: " + response.json \ "ip")
    }
  }

  def testAuth = Action.async {
    HMSApi.authenticate.map {
      case Some(token) => Ok(token.Access_Token)
      case None => Ok("Error")
    }
  }

  def testShows(channelId: String, stationId: String) = Action.async {
    HMSApi.getShows(stationId, channelId).map {
      case Some(shows) => Ok(Json.prettyPrint(shows))
      case None => Ok(Json.obj("status" -> "error"))
    }
  }

  def startPorcess = Action.async {

    showCrawler ! new StartProcess

    Future(Ok(Json.prettyPrint(Json.obj("status" -> "process started"))))
  }

  def checkApp = Action.async {
    HMSApi.authenticate.flatMap {
      case Some(token) =>
        HMSApi.getCurrentShow("ODF", "SAT").map {
          case Some(show) =>
            Ok(Json.obj("status" -> "OK"))
          case None =>
            BadRequest(Json.obj("status" -> "KO", "message" -> "HMS ccBroadcast API Down"))
        }
      case None =>
        Future(BadRequest(Json.obj("status" -> "KO", "message" -> "HMS Auth API down")))
    }
  }
}