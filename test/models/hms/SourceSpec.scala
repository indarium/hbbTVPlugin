package models.hms

import models.hms.util.{OverlayHelper, SourceHelper}
import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-17
  */
class SourceSpec extends Specification with PlayRunners {

  "Json library" should {

    "convert object to Json" in {
      running(FakeApplication()) {

        // prepare
        val source = SourceHelper.default(SourceHelper.DEFAULT_ID, SourceHelper.DEFAULT_PROFILE)

        // test
        val json = Json.toJson(source)

        // verify
        (json \ "ID").as[Long] mustEqual source.ID
        (json \ "SourceName").asOpt[String] mustEqual source.SourceName
        (json \ "StartOffset").asOpt[String] mustEqual source.StartOffset
        (json \ "EndOffset").asOpt[String] mustEqual source.EndOffset
        (json \ "DestinationName").as[String] mustEqual source.DestinationName
        (json \ "Overlays").asOpt[List[Overlay]] mustEqual source.Overlays
        (json \ "Profile").as[String] mustEqual source.Profile

      }
    }

    "convert minimum object to Json" in {
      running(FakeApplication()) {

        // prepare
        val source = SourceHelper.defaultMinimum

        // test
        val json = Json.toJson(source)

        // verify
        (json \ "ID").as[Long] mustEqual source.ID
        (json \ "SourceName").asOpt[String] mustEqual None
        (json \ "StartOffset").asOpt[String] mustEqual None
        (json \ "EndOffset").asOpt[String] mustEqual None
        (json \ "DestinationName").as[String] mustEqual source.DestinationName
        (json \ "Overlays").asOpt[List[Overlay]] mustEqual None
        (json \ "Profile").as[String] mustEqual source.Profile

      }
    }

    "convert Json to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(SourceHelper.defaultJson)

        // test
        val source = json.validate[Source]

        // verify
        source.get.ID mustEqual (json \ "ID").as[Long]
        source.get.SourceName mustEqual Some((json \ "SourceName").as[String])
        source.get.StartOffset mustEqual Some((json \ "StartOffset").as[String])
        source.get.EndOffset mustEqual Some((json \ "EndOffset").as[String])
        source.get.DestinationName mustEqual (json \ "DestinationName").as[String]
        source.get.Overlays mustEqual Some((json \ "Overlays").as[List[Overlay]])
        source.get.Profile mustEqual (json \ "Profile").as[String]

      }
    }

    "convert minimum Json to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(SourceHelper.defaultJsonMinimum)

        // test
        val source = json.validate[Source]

        // verify
        source.get.ID mustEqual (json \ "ID").as[Long]
        source.get.SourceName mustEqual None
        source.get.StartOffset mustEqual None
        source.get.EndOffset mustEqual None
        source.get.DestinationName mustEqual (json \ "DestinationName").as[String]
        source.get.Overlays mustEqual None
        source.get.Profile mustEqual (json \ "Profile").as[String]

      }
    }

  }

}
