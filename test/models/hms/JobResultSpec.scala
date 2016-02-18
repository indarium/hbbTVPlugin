package models.hms

import models.hms.util.JobResultHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-18
  */
class JobResultSpec extends Specification with PlayRunners {

  "Json library" should {

    "convert object to json" in {
      running(FakeApplication()) {

        // prepare
        val jobResult = JobResultHelper.defaultObject

        // test
        val json = Json.toJson(jobResult)

        // verify
        (json \ "ID").as[Long] mustEqual jobResult.ID
        (json \ "Result").as[String] mustEqual jobResult.Result
        (json \ "VerboseResult").as[String] mustEqual jobResult.VerboseResult

      }
    }

    "convert json to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(JobResultHelper.defaultJson)

        // test
        val jobResult = json.validate[JobResult].get

        // verify
        jobResult.ID mustEqual (json \ "ID").as[Long]
        jobResult.Result mustEqual (json \ "Result").as[String]
        jobResult.VerboseResult mustEqual (json \ "VerboseResult").as[String]

      }
    }

  }

}
