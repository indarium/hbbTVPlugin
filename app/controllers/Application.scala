package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.ws.{WSResponse, WSRequestHolder, WSAuthScheme, WS}
import play.api.Play.current
import play.api.libs.json._
import play.api.http.ContentTypeOf
import play.api.libs.ws.ning.NingWSRequestHolder
import helper.{HMSApi, MongoDB}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Application extends Controller {

  def index = Action {

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