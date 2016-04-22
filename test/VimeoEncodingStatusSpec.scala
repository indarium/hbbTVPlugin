import constants.VimeoEncodingStatusSystem._
import models.{Show, ShowHelper}
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

        // prepare
        val show = ShowHelper.defaultObject("channelId", "stationId", -1L)
        val json = Json.toJson(show)
        println(json)
        val jsonString = json.toString()

        // test
        val vimeo = (json \ "vimeoEncodingStatus").as[String]

        // verify
        vimeo mustEqual show.vimeoEncodingStatus.get.name

      }
    }

    "parse a Show JSON into a Show Scala" in {
      running(FakeApplication()) {
        val json: JsValue = JsObject(Seq(
          "_id" -> JsObject(Seq("$oid" -> JsString("56be0905e667f841bc321cc4"))),
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
          "vimeoEncodingStatus" -> JsString(DONE.name),
          "s3Name" -> JsString("s3Name")
        ))

        val show = json.validate[Show]
        println(show.get)

        show.get.stationName mustEqual "stationName"
      }
    }
  }
}
