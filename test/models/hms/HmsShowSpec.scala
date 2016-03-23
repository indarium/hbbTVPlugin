package models.hms

import constants.JsonConstants
import models.hms.util.HmsShowHelper
import org.specs2.mutable.Specification
import play.api.libs.json.{JsError, JsResult, Json}
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-03-23
  */
class HmsShowSpec extends Specification with PlayRunners {

  "Json library" should {

    "convert object to json" in {
      running(FakeApplication()) {

        // prepare
        val hmsShow = HmsShowHelper.defaultObject

        // test
        val json = Json.toJson(hmsShow)

        // verify
        (json \ "ID").as[Int] mustEqual hmsShow.ID
        (json \ "Name").asOpt[String] mustEqual hmsShow.Name
        (json \ "UTCEnd").as[String] mustEqual hmsShow.UTCEnd.toString(JsonConstants.dateFormat)
        (json \ "DownloadURL").asOpt[String] mustEqual hmsShow.DownloadURL
        (json \ "ChannelID").as[Long] mustEqual hmsShow.ChannelID
        (json \ "ParentID").as[Long] mustEqual hmsShow.ParentID

      }

    }

    "convert minimum object to json" in {
      running(FakeApplication()) {

        // prepare
        val hmsShow = HmsShowHelper.defaultMinimumObject

        // test
        val json = Json.toJson(hmsShow)

        // verify
        (json \ "ID").as[Int] mustEqual hmsShow.ID
        (json \ "Name").asOpt[String] mustEqual hmsShow.Name
        (json \ "UTCEnd").as[String] mustEqual hmsShow.UTCEnd.toString(JsonConstants.dateFormat)
        (json \ "DownloadURL").asOpt[String] mustEqual hmsShow.DownloadURL
        (json \ "ChannelID").as[Long] mustEqual hmsShow.ChannelID
        (json \ "ParentID").as[Long] mustEqual hmsShow.ParentID

      }

    }

    "convert json to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(HmsShowHelper.defaultJson)

        // test
        json.validate[HmsShow] match {

          // verify
          case jsError: JsError => throw new IllegalStateException(s"failed to valide json: json=$json, jsError=$jsError")

          case jsResult: JsResult[HmsShow] =>
            val hmsShow = jsResult.get
            hmsShow.ID mustEqual (json \ "ID").as[Int]
            hmsShow.Name mustEqual (json \ "Name").asOpt[String]
            hmsShow.UTCEnd.toString(JsonConstants.dateFormat) mustEqual (json \ "UTCEnd").as[String]
            hmsShow.DownloadURL mustEqual (json \ "DownloadURL").asOpt[String]
            hmsShow.ChannelID mustEqual (json \ "ChannelID").as[Long]
            hmsShow.ParentID mustEqual (json \ "ParentID").as[Long]

        }

      }
    }

    "convert minimum json to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(HmsShowHelper.defaultMinimumJson)

        // test
        json.validate[HmsShow] match {

          // verify
          case jsError: JsError => throw new IllegalStateException(s"failed to valide json: json=$json, jsError=$jsError")

          case jsResult: JsResult[HmsShow] =>
            val hmsShow = jsResult.get
            hmsShow.ID mustEqual (json \ "ID").as[Int]
            hmsShow.Name must beNone
            hmsShow.UTCEnd.toString(JsonConstants.dateFormat) mustEqual (json \ "UTCEnd").as[String]
            hmsShow.DownloadURL must beNone
            hmsShow.ChannelID mustEqual (json \ "ChannelID").as[Long]
            hmsShow.ParentID mustEqual (json \ "ParentID").as[Long]

        }

      }
    }

  }

}
