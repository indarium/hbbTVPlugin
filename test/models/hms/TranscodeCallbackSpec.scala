package models.hms

import models.dto.ShowMetaData
import models.hms.util.TranscodeCallbackHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{FakeApplication, PlayRunners}
import reactivemongo.bson.BSON

/**
  * author: cvandrei
  * since: 2016-02-19
  */
class TranscodeCallbackSpec extends Specification with PlayRunners {

  "json library" should {

    "convert object (queued and w/o ShowMetaData) to json" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.queuedObjectWithoutMeta(-1L)

        // test
        val json = Json.toJson(processing)

        // verify
        val transcodeCallback = json.as[TranscodeCallback]
        transcodeCallback.ID mustEqual processing.ID
        transcodeCallback.VerboseMessage mustEqual processing.VerboseMessage
        transcodeCallback.Status mustEqual processing.Status
        transcodeCallback.StatusValue.isEmpty mustEqual true
        transcodeCallback.StatusUnit.isEmpty mustEqual true
        transcodeCallback.DownloadSource.isEmpty mustEqual true
        transcodeCallback.meta.isEmpty mustEqual true

      }
    }

    "convert object (queued and w/ ShowMetaData) to json" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.queuedObjectWithMeta(-1L)

        // test
        val json = Json.toJson(processing)

        // verify
        (json \ "ID").as[Long] mustEqual processing.ID
        (json \ "VerboseMessage").asOpt[String] mustEqual processing.VerboseMessage
        (json \ "Status").as[String] mustEqual processing.Status
        (json \ "StatusValue").asOpt[Int] mustEqual None
        (json \ "StatusUnit").asOpt[String] mustEqual None
        (json \ "DownloadSource").asOpt[String] mustEqual None
        (json \ "meta").asOpt[ShowMetaData].get mustEqual processing.meta.get

      }
    }

    "convert object (processing and w/o ShowMetaData) to json" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.processingObjectWithoutMeta(-1L)

        // test
        val json = Json.toJson(processing)

        // verify
        val transcodeCallback = json.as[TranscodeCallback]
        transcodeCallback.ID mustEqual processing.ID
        transcodeCallback.VerboseMessage mustEqual processing.VerboseMessage
        transcodeCallback.Status mustEqual processing.Status
        transcodeCallback.StatusValue mustEqual processing.StatusValue
        transcodeCallback.StatusUnit mustEqual processing.StatusUnit
        transcodeCallback.DownloadSource.isEmpty mustEqual true
        transcodeCallback.meta.isEmpty mustEqual true

      }
    }

    "convert object (processing and w/ ShowMetaData) to json" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.processingObjectWithMeta(-1L)

        // test
        val json = Json.toJson(processing)

        // verify
        (json \ "ID").as[Long] mustEqual processing.ID
        (json \ "VerboseMessage").asOpt[String] mustEqual processing.VerboseMessage
        (json \ "Status").as[String] mustEqual processing.Status
        (json \ "StatusValue").asOpt[Int] mustEqual processing.StatusValue
        (json \ "StatusUnit").asOpt[String] mustEqual processing.StatusUnit
        (json \ "DownloadSource").asOpt[String] mustEqual None
        (json \ "meta").asOpt[ShowMetaData].get mustEqual processing.meta.get

      }
    }

    "convert object (finished and w/o ShowMetaData) to json" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.finishedObjectWithoutMeta(-1L)

        // test
        val json = Json.toJson(processing)

        // verify
        val transcodeCallback = json.as[TranscodeCallback]
        transcodeCallback.ID mustEqual processing.ID
        transcodeCallback.VerboseMessage mustEqual processing.VerboseMessage
        transcodeCallback.Status mustEqual processing.Status
        transcodeCallback.StatusValue mustEqual None
        transcodeCallback.StatusUnit mustEqual None
        transcodeCallback.DownloadSource mustEqual processing.DownloadSource
        transcodeCallback.meta.isEmpty mustEqual true

      }
    }

    "convert object (finished and w/ ShowMetaData) to json" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.finishedObjectWithMeta(-1L)

        // test
        val json = Json.toJson(processing)

        // verify
        (json \ "ID").as[Long] mustEqual processing.ID
        (json \ "VerboseMessage").asOpt[String] mustEqual processing.VerboseMessage
        (json \ "Status").as[String] mustEqual processing.Status
        (json \ "StatusValue").asOpt[Int] mustEqual None
        (json \ "StatusUnit").asOpt[String] mustEqual None
        (json \ "DownloadSource").asOpt[String] mustEqual processing.DownloadSource
        (json \ "meta").asOpt[ShowMetaData].get mustEqual processing.meta.get

      }
    }

    "convert json (queued and w/o ShowMetaData) to object" in {
      running(FakeApplication()) {

        // prepare
        val id = 4945260944371534587L
        val json = Json.parse(TranscodeCallbackHelper.queuedJsonWithoutMeta(id))

        // test
        val transcodeCallback = json.validate[TranscodeCallback].get

        // verify
        transcodeCallback.ID mustEqual id
        transcodeCallback.VerboseMessage mustEqual (json \ "VerboseMessage").asOpt[String]
        transcodeCallback.Status mustEqual (json \ "Status").as[String]
        transcodeCallback.StatusValue mustEqual None
        transcodeCallback.StatusUnit mustEqual None
        transcodeCallback.DownloadSource mustEqual None
        transcodeCallback.meta mustEqual None

      }
    }

    "convert json (queued and w/ ShowMetaData) to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(TranscodeCallbackHelper.queuedJsonWithMeta(-1L))

        // test
        val transcodeCallback = json.validate[TranscodeCallback].get

        // verify
        transcodeCallback.ID mustEqual (json \ "ID").as[Long]
        transcodeCallback.VerboseMessage mustEqual (json \ "VerboseMessage").asOpt[String]
        transcodeCallback.Status mustEqual (json \ "Status").as[String]
        transcodeCallback.StatusValue mustEqual None
        transcodeCallback.StatusUnit mustEqual None
        transcodeCallback.DownloadSource mustEqual None
        transcodeCallback.meta.get mustEqual (json \ "meta").asOpt[ShowMetaData].get

      }
    }

    "convert json (processing and w/o ShowMetaData) to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(TranscodeCallbackHelper.processingJsonWithoutMeta(-1L))

        // test
        val transcodeCallback = json.validate[TranscodeCallback].get

        // verify
        transcodeCallback.ID mustEqual (json \ "ID").as[Long]
        transcodeCallback.VerboseMessage mustEqual (json \ "VerboseMessage").asOpt[String]
        transcodeCallback.Status mustEqual (json \ "Status").as[String]
        transcodeCallback.StatusValue mustEqual (json \ "StatusValue").asOpt[Int]
        transcodeCallback.StatusUnit mustEqual (json \ "StatusUnit").asOpt[String]
        transcodeCallback.DownloadSource mustEqual None
        transcodeCallback.meta mustEqual None

      }
    }

    "convert json (processing and w/ ShowMetaData) to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(TranscodeCallbackHelper.processingJsonWithMeta(-1L))

        // test
        val transcodeCallback = json.validate[TranscodeCallback].get

        // verify
        transcodeCallback.ID mustEqual (json \ "ID").as[Long]
        transcodeCallback.VerboseMessage mustEqual (json \ "VerboseMessage").asOpt[String]
        transcodeCallback.Status mustEqual (json \ "Status").as[String]
        transcodeCallback.StatusValue mustEqual (json \ "StatusValue").asOpt[Int]
        transcodeCallback.StatusUnit mustEqual (json \ "StatusUnit").asOpt[String]
        transcodeCallback.DownloadSource mustEqual None
        transcodeCallback.meta.get mustEqual (json \ "meta").asOpt[ShowMetaData].get

      }
    }

    "convert json (finished and w/o ShowMetaData) to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(TranscodeCallbackHelper.finishedJsonWithoutMeta(-1L))

        // test
        val transcodeCallback = json.validate[TranscodeCallback].get

        // verify
        transcodeCallback.ID mustEqual (json \ "ID").as[Long]
        transcodeCallback.VerboseMessage mustEqual (json \ "VerboseMessage").asOpt[String]
        transcodeCallback.Status mustEqual (json \ "Status").as[String]
        transcodeCallback.StatusValue mustEqual None
        transcodeCallback.StatusUnit mustEqual None
        transcodeCallback.DownloadSource mustEqual (json \ "DownloadSource").asOpt[String]
        transcodeCallback.meta mustEqual None

      }
    }

    "convert json (finished and w/ ShowMetaData) to object" in {
      running(FakeApplication()) {

        // prepare
        val id = 5508256850023639342L
        val json = Json.parse(TranscodeCallbackHelper.finishedJsonWithMeta(id))

        // test
        val transcodeCallback = json.validate[TranscodeCallback].get

        // verify
        transcodeCallback.ID mustEqual (json \ "ID").as[Long]
        transcodeCallback.VerboseMessage mustEqual (json \ "VerboseMessage").asOpt[String]
        transcodeCallback.Status mustEqual (json \ "Status").as[String]
        transcodeCallback.StatusValue mustEqual None
        transcodeCallback.StatusUnit mustEqual None
        transcodeCallback.DownloadSource mustEqual (json \ "DownloadSource").asOpt[String]
        transcodeCallback.meta.get mustEqual (json \ "meta").asOpt[ShowMetaData].get

      }
    }

    "convert object (queued and w/ ShowMetaData) to bson" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.queuedObjectWithMeta(5678010794297290347L)

        // test
        val bson = BSON.writeDocument(processing)

        // verify
        val o = BSON.readDocument[TranscodeCallback](bson)
        o mustEqual processing
        o.meta mustEqual processing.meta

      }
    }

  }

}
