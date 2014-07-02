package controllers

import java.io.File

import actors.StorageBackendActor
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import helper.HMSApi
import play.api.Play.current
import play.api.libs.ws.{WS, WSRequestHolder}
import play.api.mvc._
import play.libs.Akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Application extends Controller {

  def index = Action {

    lazy val s3Uploader = Akka.system.actorOf(Props[StorageBackendActor])

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

  def testIt = Action.async { request =>

    /*HMSApi.authenticate.map { resultData =>
      Ok(resultData.toString)
    }*/

    HMSApi.getShows("OSF").map { resultData =>
      Ok("hallo" + resultData.toString)
    }
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
}