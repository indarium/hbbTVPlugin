package models.vimeo.video

import models.hms.{Source, Transcode}
import models.vimeo.video.util.TranscodeHelper
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
        (json \ "Collapsed").asOpt[Boolean] mustEqual transcode.Collapsed
        (json \ "CollapsedName").asOpt[String] mustEqual transcode.CollapsedName
        (json \ "DownloadProvision").as[String] mustEqual transcode.DownloadProvision
        (json \ "PushFinishedNotification").as[Boolean] mustEqual transcode.PushFinishedNotification
        (json \ "PushErrorNotification").as[Boolean] mustEqual transcode.PushErrorNotification
        (json \ "PushStatusNotification").as[Boolean] mustEqual transcode.PushStatusNotification
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
        (json \ "Collapsed").asOpt[Boolean] mustEqual None
        (json \ "CollapsedName").asOpt[String] mustEqual None
        (json \ "DownloadProvision").as[String] mustEqual transcode.DownloadProvision
        (json \ "PushFinishedNotification").as[Boolean] mustEqual transcode.PushFinishedNotification
        (json \ "PushErrorNotification").as[Boolean] mustEqual transcode.PushErrorNotification
        (json \ "PushStatusNotification").as[Boolean] mustEqual transcode.PushStatusNotification
        (json \ "PushNotificationCallback").as[String] mustEqual transcode.PushNotificationCallback

      }
    }

    "convert Json to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(TranscodeHelper.defaultJson)

        // test
        val transcode = json.validate[Transcode]

        // verify
        transcode.get.SourceType mustEqual (json \ "SourceType").as[String]
        transcode.get.Sources mustEqual (json \ "Sources").as[List[Source]]
        transcode.get.Collapsed mustEqual Some((json \ "Collapsed").as[Boolean])
        transcode.get.CollapsedName mustEqual Some((json \ "CollapsedName").as[String])
        transcode.get.DownloadProvision mustEqual (json \ "DownloadProvision").as[String]
        transcode.get.PushFinishedNotification mustEqual (json \ "PushFinishedNotification").as[Boolean]
        transcode.get.PushErrorNotification mustEqual (json \ "PushErrorNotification").as[Boolean]
        transcode.get.PushStatusNotification mustEqual (json \ "PushStatusNotification").as[Boolean]
        transcode.get.PushNotificationCallback mustEqual (json \ "PushNotificationCallback").as[String]

      }
    }

    "convert minimum Json to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(TranscodeHelper.defaultJsonMinimum)

        // test
        val transcode = json.validate[Transcode]

        // verify
        transcode.get.SourceType mustEqual (json \ "SourceType").as[String]
        transcode.get.Sources mustEqual (json \ "Sources").as[List[Source]]
        transcode.get.Collapsed mustEqual None
        transcode.get.CollapsedName mustEqual None
        transcode.get.DownloadProvision mustEqual (json \ "DownloadProvision").as[String]
        transcode.get.PushFinishedNotification mustEqual (json \ "PushFinishedNotification").as[Boolean]
        transcode.get.PushErrorNotification mustEqual (json \ "PushErrorNotification").as[Boolean]
        transcode.get.PushStatusNotification mustEqual (json \ "PushStatusNotification").as[Boolean]
        transcode.get.PushNotificationCallback mustEqual (json \ "PushNotificationCallback").as[String]

      }
    }

  }

}
