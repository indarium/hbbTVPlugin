package models.hms

import models.hms.util.TranscodeHelper
import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-17
  */
class TranscodeSpec extends Specification with PlayRunners {

  "Json library" should {

    "convert object to Json" in {
      running(FakeApplication()) {

        // prepare
        val transcode = TranscodeHelper.default

        // test
        val json = Json.toJson(transcode)

        // verify
        (json \ "SourceType").as[String] mustEqual transcode.SourceType
        (json \ "Sources").as[List[Source]] mustEqual transcode.Sources
        (json \ "Collapsed").asOpt[String].get mustEqual transcode.Collapsed.get.toString
        (json \ "CollapsedName").asOpt[String] mustEqual transcode.CollapsedName
        (json \ "DownloadProvision").as[String] mustEqual transcode.DownloadProvision
        (json \ "PushFinishedNotification").as[String] mustEqual transcode.PushFinishedNotification.toString
        (json \ "PushErrorNotification").as[String] mustEqual transcode.PushErrorNotification.toString
        (json \ "PushStatusNotification").as[String] mustEqual transcode.PushStatusNotification.toString
        (json \ "PushNotificationCallback").as[String] mustEqual transcode.PushNotificationCallback

      }
    }

    "convert minimum object to Json" in {
      running(FakeApplication()) {

        // prepare
        val transcode = TranscodeHelper.defaultMinimum

        // test
        val json = Json.toJson(transcode)

        // verify
        (json \ "SourceType").as[String] mustEqual transcode.SourceType
        (json \ "Sources").as[List[Source]] mustEqual transcode.Sources
        (json \ "Collapsed").asOpt[String] mustEqual None
        (json \ "CollapsedName").asOpt[String] mustEqual None
        (json \ "DownloadProvision").as[String] mustEqual transcode.DownloadProvision
        (json \ "PushFinishedNotification").as[String] mustEqual transcode.PushFinishedNotification.toString
        (json \ "PushErrorNotification").as[String] mustEqual transcode.PushErrorNotification.toString
        (json \ "PushStatusNotification").as[String] mustEqual transcode.PushStatusNotification.toString
        (json \ "PushNotificationCallback").as[String] mustEqual transcode.PushNotificationCallback

      }
    }

    "convert Json to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(TranscodeHelper.defaultJson)

        // test
        val transcode = json.validate[Transcode].get

        // verify
        transcode.SourceType mustEqual (json \ "SourceType").as[String]
        transcode.Sources mustEqual (json \ "Sources").as[List[Source]]
        transcode.Collapsed mustEqual Some((json \ "Collapsed").as[String].toBoolean)
        transcode.CollapsedName mustEqual Some((json \ "CollapsedName").as[String])
        transcode.DownloadProvision mustEqual (json \ "DownloadProvision").as[String]
        transcode.PushFinishedNotification mustEqual (json \ "PushFinishedNotification").as[String].toBoolean
        transcode.PushErrorNotification mustEqual (json \ "PushErrorNotification").as[String].toBoolean
        transcode.PushStatusNotification mustEqual (json \ "PushStatusNotification").as[String].toBoolean
        transcode.PushNotificationCallback mustEqual (json \ "PushNotificationCallback").as[String]

      }
    }

    "convert minimum Json to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(TranscodeHelper.defaultJsonMinimum)

        // test
        val transcode = json.validate[Transcode].get

        // verify
        transcode.SourceType mustEqual (json \ "SourceType").as[String]
        transcode.Sources mustEqual (json \ "Sources").as[List[Source]]
        transcode.Collapsed mustEqual None
        transcode.CollapsedName mustEqual None
        transcode.DownloadProvision mustEqual (json \ "DownloadProvision").as[String]
        transcode.PushFinishedNotification mustEqual (json \ "PushFinishedNotification").as[String].toBoolean
        transcode.PushErrorNotification mustEqual (json \ "PushErrorNotification").as[String].toBoolean
        transcode.PushStatusNotification mustEqual (json \ "PushStatusNotification").as[String].toBoolean
        transcode.PushNotificationCallback mustEqual (json \ "PushNotificationCallback").as[String]

      }
    }

  }

}
