package models.dto

import models.dto.util.ShowMetaDataHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-19
  */
class ShowMetaDataSpec extends Specification with PlayRunners {

  "json library" should {

    "convert object to json" in {
      running(FakeApplication()) {

        // prepare
        val meta = ShowMetaDataHelper.defaultObject("SAT", "MV1", -123L)

        // test
        val json = Json.toJson(meta)

        // verify
        (json \ "channelId").as[String] mustEqual meta.channelId
        (json \ "stationId").as[String] mustEqual meta.stationId
        (json \ "showId").asOpt[Long] mustEqual meta.showId

      }
    }

  }

}
