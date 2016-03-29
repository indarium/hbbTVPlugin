package models.hms

import constants.JsonConstants
import models.dto.ShowMetaData
import models.hms.util.TranscodeCallbackHelper
import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}
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
        val queued = TranscodeCallbackHelper.queuedObjectWithoutMeta(-1L)

        // test
        val json = Json.toJson(queued)

        // verify
        val transcodeCallback = json.as[TranscodeCallback]
        transcodeCallback mustEqual queued

      }
    }

    "convert object (queued and w/ ShowMetaData) to json" in {
      running(FakeApplication()) {

        // prepare
        val queued = TranscodeCallbackHelper.queuedObjectWithMeta(-1L)

        // test
        val json = Json.toJson(queued)

        // verify
        val transcodeCallback = json.as[TranscodeCallback]
        transcodeCallback mustEqual queued

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
        transcodeCallback mustEqual processing

      }
    }

    "convert object (processing and w/ ShowMetaData) to json" in {
      running(FakeApplication()) {

        // prepare
        val processing = TranscodeCallbackHelper.processingObjectWithMeta(-1L)

        // test
        val json = Json.toJson(processing)

        // verify
        val transcodeCallback = json.as[TranscodeCallback]
        transcodeCallback mustEqual processing

      }
    }

    "convert object (finished and w/o ShowMetaData) to json" in {
      running(FakeApplication()) {

        // prepare
        val finished = TranscodeCallbackHelper.finishedObjectWithoutMeta(-1L)

        // test
        val json = Json.toJson(finished)

        // verify
        val transcodeCallback = json.as[TranscodeCallback]
        transcodeCallback mustEqual finished

      }
    }

    "convert object (finished and w/ ShowMetaData) to json" in {
      running(FakeApplication()) {

        // prepare
        val finished = TranscodeCallbackHelper.finishedObjectWithMeta(-1L)

        // test
        val json = Json.toJson(finished)

        // verify
        val transcodeCallback = json.as[TranscodeCallback]
        transcodeCallback mustEqual finished

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
        transcodeCallback.StatusValue must beNone
        transcodeCallback.StatusUnit must beNone
        transcodeCallback.DownloadSource must beNone
        transcodeCallback.meta must beNone
        verifyCreatedAndModified(transcodeCallback, json)

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
        transcodeCallback.StatusValue must beNone
        transcodeCallback.StatusUnit must beNone
        transcodeCallback.DownloadSource must beNone
        transcodeCallback.meta.get mustEqual (json \ "meta").asOpt[ShowMetaData].get
        verifyCreatedAndModified(transcodeCallback, json)

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
        transcodeCallback.DownloadSource must beNone
        transcodeCallback.meta must beNone
        verifyCreatedAndModified(transcodeCallback, json)

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
        transcodeCallback.DownloadSource must beNone
        transcodeCallback.meta.get mustEqual (json \ "meta").asOpt[ShowMetaData].get
        verifyCreatedAndModified(transcodeCallback, json)

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
        transcodeCallback.StatusValue must beNone
        transcodeCallback.StatusUnit must beNone
        transcodeCallback.DownloadSource mustEqual (json \ "DownloadSource").asOpt[String]
        transcodeCallback.meta must beNone
        verifyCreatedAndModified(transcodeCallback, json)

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
        transcodeCallback.StatusValue must beNone
        transcodeCallback.StatusUnit must beNone
        transcodeCallback.DownloadSource mustEqual (json \ "DownloadSource").asOpt[String]
        transcodeCallback.meta.get mustEqual (json \ "meta").asOpt[ShowMetaData].get
        verifyCreatedAndModified(transcodeCallback, json)

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

  /*
   * TEST HELPERS
   ********************************************************************************************************************/

  private def verifyCreatedAndModified(transcodeCallback: TranscodeCallback, json: JsValue) = {
    transcodeCallback.created.get.toString(JsonConstants.dateFormat) mustEqual (json \ "created").as[String]
    transcodeCallback.modified.get.toString(JsonConstants.dateFormat) mustEqual (json \ "modified").as[String]
  }

}
