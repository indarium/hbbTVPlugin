import constants.VimeoEncodingStatusSystem._
import models.Show
import org.specs2.mutable.Specification
import play.api.libs.json._
import play.api.test.{FakeApplication, PlayRunners}

/**
  * Created by thiago on 1/27/16.
  */
class VimeoEncodingStatusSpec extends Specification with PlayRunners {

  "The Json library" should {

    "parse Show Scala into a Show JSON" in {
      running(FakeApplication()) {
        val show = Show(
//          None,
          "stationId",
          "stationName",
          "stationLogoUrl",
          stationLogoDisplay = false,
          "stationMainColor",
          "channelId",
          "channelName",
          showId = -1L,
          "showTitle",
          "showSourceTitle",
          "showSubtitle",
          "showLogoUrl",
          Some("showVideoHDUrl"),
          "showVideoSDUrl",
          "channelBroadcastInfo",
          "rootPortalUrl",
          vimeoId = Some(-1L),
          vimeoEncodingStatus = Some(DONE)
        )

        val json = Json.toJson(show)
        val jsonString = json.toString()

        println(json)

        val vimeo = (json \ "vimeoEncodingStatus").as[String]
        vimeo mustEqual "DONE"
      }
    }

    "parse a Show JSON into a Show Scala" in {
      running(FakeApplication()) {
        val json: JsValue = JsObject(Seq(
          "stationId" -> JsString("stationId"),
          "stationName" -> JsString("stationName"),
          "stationLogoUrl" -> JsString("stationLogoUrl"),
          "stationLogoDisplay" -> JsBoolean(false),
          "stationMainColor" -> JsString("stationMainColor"),
          "channelId" -> JsString("channelId"),
          "channelName" -> JsString("channelName"),
          "showId" -> JsNumber(-1L),
          "showTitle" -> JsString("showTitle"),
          "showSourceTitle" -> JsString("showSourceTitle"),
          "showSubtitle" -> JsString("showLogoUrl"),
          "showLogoUrl" -> JsString("showLogoUrl"),
          "showVideoHDUrl" -> JsString("showVideoHDUrl"),
          "showVideoSDUrl" -> JsString("showVideoSDUrl"),
          "channelBroadcastInfo" -> JsString("channelBroadcastInfo"),
          "rootPortalURL" -> JsString("rootPortalURL"),
          "vimeoId" -> JsNumber(-1L),
          "vimeoEncodingStatus" -> JsString(DONE.name)
        ))

        val show = json.validate[Show]
        println(show.get)

        show.get.stationName mustEqual "stationName"
      }
    }
  }
}
