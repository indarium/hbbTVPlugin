package controllers

import java.net.URL

import actors.ShowProcessingActor
import akka.actor.Props
import akka.util.Timeout
import com.amazonaws.auth.BasicAWSCredentials
import helper._
import play.api.Play
import play.api.Play.current
import play.api.libs.json.JsObject
import play.api.libs.ws.{WS, WSRequestHolder}
import play.api.mvc._
import play.api.libs.json.Json
import play.libs.Akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Application extends Controller {

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

  def testShow(channelId: String, stationId: String) = Action.async {
    val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
    val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")
    val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
    val s3Backend: S3Backend = new S3Backend(credentials, Play.configuration.getString("aws.bucket").get)

    val showProcessingActor = Akka.system.actorOf(Props(new ShowProcessingActor(s3Backend)))


    HMSApi.getCurrentShow(stationId, channelId).map {
      case Some(show) =>
        var meta = new ShowMetaData(stationId, channelId)

        meta.showId = Some(show.ID.toString)
        meta.showTitle = show.Name
        meta.sourceVideoUrl = Some(new URL(show.DownloadURL.getOrElse("")))
        showProcessingActor ! meta
        Ok(show.toString)
      case None => Ok("Error")
    }
  }

}