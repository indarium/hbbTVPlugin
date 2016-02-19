package models.hms

import models.hms.util.TranscodeCallbackHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-19
  */
class TranscodeCallbackSpec extends Specification with PlayRunners {

  "json library" should {

    "convert object (queued) to json" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.queuedObject(-1L)

        // test
        val json = Json.toJson(processing)

        // verify
        (json \ "ID").as[Long] mustEqual processing.ID
        (json \ "VerboseMessage").as[String] mustEqual processing.VerboseMessage
        (json \ "Status").as[String] mustEqual processing.Status
        (json \ "StatusValue").asOpt[Int] mustEqual None
        (json \ "StatusUnit").asOpt[String] mustEqual None
        (json \ "DownloadSource").asOpt[String] mustEqual None

      }
    }

    "convert object (processing) to json" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.processingObject(-1L)

        // test
        val json = Json.toJson(processing)

        // verify
        (json \ "ID").as[Long] mustEqual processing.ID
        (json \ "VerboseMessage").as[String] mustEqual processing.VerboseMessage
        (json \ "Status").as[String] mustEqual processing.Status
        (json \ "StatusValue").asOpt[Int] mustEqual processing.StatusValue
        (json \ "StatusUnit").asOpt[String] mustEqual processing.StatusUnit
        (json \ "DownloadSource").asOpt[String] mustEqual None

      }
    }

    "convert object (finished) to json" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.finishedObject(-1L)

        // test
        val json = Json.toJson(processing)

        // verify
        (json \ "ID").as[Long] mustEqual processing.ID
        (json \ "VerboseMessage").as[String] mustEqual processing.VerboseMessage
        (json \ "Status").as[String] mustEqual processing.Status
        (json \ "StatusValue").asOpt[Int] mustEqual None
        (json \ "StatusUnit").asOpt[String] mustEqual None
        (json \ "DownloadSource").asOpt[String] mustEqual processing.DownloadSource

      }
    }

    "convert json (queued) to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(TranscodeCallbackHelper.queuedJson(-1L))

        // test
        val transcodeCallback = json.validate[TranscodeCallback].get

        // verify
        transcodeCallback.ID mustEqual (json \ "ID").as[Long]
        transcodeCallback.VerboseMessage mustEqual (json \ "VerboseMessage").as[String]
        transcodeCallback.Status mustEqual (json \ "Status").as[String]
        transcodeCallback.StatusValue mustEqual None
        transcodeCallback.StatusUnit mustEqual None
        transcodeCallback.DownloadSource mustEqual None

      }
    }

    "convert json (processing) to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(TranscodeCallbackHelper.processingJson(-1L))

        // test
        val transcodeCallback = json.validate[TranscodeCallback].get

        // verify
        transcodeCallback.ID mustEqual (json \ "ID").as[Long]
        transcodeCallback.VerboseMessage mustEqual (json \ "VerboseMessage").as[String]
        transcodeCallback.Status mustEqual (json \ "Status").as[String]
        transcodeCallback.StatusValue mustEqual (json \ "StatusValue").asOpt[Int]
        transcodeCallback.StatusUnit mustEqual (json \ "StatusUnit").asOpt[String]
        transcodeCallback.DownloadSource mustEqual None

      }
    }

    "convert json (finished) to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(TranscodeCallbackHelper.finishedJson(-1L))

        // test
        val transcodeCallback = json.validate[TranscodeCallback].get

        // verify
        transcodeCallback.ID mustEqual (json \ "ID").as[Long]
        transcodeCallback.VerboseMessage mustEqual (json \ "VerboseMessage").as[String]
        transcodeCallback.Status mustEqual (json \ "Status").as[String]
        transcodeCallback.StatusValue mustEqual None
        transcodeCallback.StatusUnit mustEqual None
        transcodeCallback.DownloadSource mustEqual (json \ "DownloadSource").asOpt[String]

      }
    }

  }

}
