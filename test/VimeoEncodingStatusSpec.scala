import constants.VimeoEncodingStatusSystem.{DONE, VimeoEncodingStatus, IN_PROGRESS}
import models.Show
import org.specs2.mutable.Specification
import play.api.libs.json.Json

/**
  * Created by thiago on 1/27/16.
  */
class VimeoEncodingStatusSpec extends Specification {

  "The Json library" should {

    "parse CASE OBJECT into a JSON" in {
      val obj = Json.toJson(IN_PROGRESS)
//      (obj \ "vimeo-encoding-status") mustEqual "IN_PROGRESS" // no key "vimeo-encoding-status" exists in resulting json
      obj.as[VimeoEncodingStatus].name mustEqual "IN_PROGRESS"
    }

    "parse JSON into a CASE OBJECT" in {

      val json = Json.obj("vimeo-encoding-status" -> "IN_PROGRESS", "$variant" -> "IN_PROGRESS")
//      val json = Json.obj("vimeo-encoding-status" -> "IN_PROGRESS") // replacing the above line with this one breaks the test
      val obj = json.asOpt[VimeoEncodingStatus]

      obj must beSome
      obj.get.name mustEqual "IN_PROGRESS"
    }

//    "parse CASE OBJECT into a Show JSON" in {
//
//      val show = new Show("stationId",
//                          "stationName",
//                          "stationLogoUrl",
//                          false, // stationLogoDisplay
//                          "stationMainColor",
//                          "channelId",
//                          "channelName",
//                          -1L, // showId
//                          "showTitle",
//                          "showSourceTitle",
//                          "showSubtitle",
//                          "showLogoUrl",
//                          Some("showVideoHDUrl"),
//                          "showVideoSDUrl",
//                          "channelBroadcastInfo",
//                          "rootPortalUrl",
//                          Some(-1L), // vimeoId
//                          Some(DONE)
//      )
//
//      val json = Json.toJson(show)
//
//      (json \ "show" \ "$variant") mustEqual("DONE")
////      (json \ "show" \ "vimeo-encoding-status") mustEqual(DONE.toString)
//
//    }

  }
}
