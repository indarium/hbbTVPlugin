package models.hms

import models.hms.util.TranscodeResponseHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-18
  */
class TranscodeResponseSpec extends Specification with PlayRunners {

  "json library" should {

    "convert object to json" in {
      running(FakeApplication()) {

        // prepare
        val transcodeResponse = TranscodeResponseHelper.defaultObject

        // test
        val json = Json.toJson(transcodeResponse)

        // verify
        (json \ "Job").as[List[JobResult]] mustEqual transcodeResponse.Job

      }
    }

    "convert json to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(TranscodeResponseHelper.defaultJson)

        // test
        val transcodeResponse = json.validate[TranscodeResponse].get

        // verify
        transcodeResponse.Job mustEqual (json \ "Job").as[List[JobResult]]

      }
    }

  }

}
