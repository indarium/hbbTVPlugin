import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import org.specs2.matcher.JsonMatchers

import play.api.libs.json.{JsString, Json}
import play.api.libs.ws.WS
import play.api.{Play, Logger}
import play.api.test._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class HMSAPISpec extends Specification {

  "HMS API" should {

    "should deliver an API token" in new WithBrowser {

      var username = Play.configuration.getString("hms.username").get
      val password = Play.configuration.getString("hms.password").get
      val apiUrl = Play.configuration.getString("hms.apiURL").get + ("/login/")

      val authData = Json.obj(
        "UserName" -> JsString(username),
        "Password" -> JsString(password)
      )

      Logger.info(authData.toString())
      val respString = WS.url(apiUrl)
        .withHeaders("x-api-version" -> "1.0")
        .withRequestTimeout(2000)
        .post(authData).map { response =>
        response.status match {
          case s if s < 300 =>
            response.json.toString()
          case _ =>
            ""
        }
      }

      Logger.info(respString.map {wups => wups} toString)
      failure("Puups")
    }
  }
}