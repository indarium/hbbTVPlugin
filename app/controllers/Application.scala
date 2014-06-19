package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.ws.{WSResponse, WSRequestHolder, WSAuthScheme, WS}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.http.ContentTypeOf
import play.api.libs.ws.ning.NingWSRequestHolder
import helper.MongoDB

object Application extends Controller {

  def index = Action.async {
    val url = "https://62.67.13.54/HMSCloud/api/login/"
    var user = "merz"
    val password = "merz"

    //val holder = WS.url(url)


    val authData = Json.obj(
      "UserName" -> JsString(user),
      "Password" -> JsString(password)
    )

    Logger.info(authData.toString())

    //holder
    WS.url(url)
      .withHeaders("x-api-version" -> "1.0")
      .withRequestTimeout(2000)
      .post(authData)
      .map { response =>
      response.status match {
        case s if s < 300 =>
          Ok(response.json \ "AccessToken")
        case _ =>
          Ok("Status: " + response.status + "/ Error: " + response.body)
      }
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