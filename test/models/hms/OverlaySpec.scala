package models.hms

import models.hms.util.OverlayHelper
import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-17
  */
class OverlaySpec extends Specification with PlayRunners {

  "Json library" should {

    "convert object to Json" in {
      running(FakeApplication()) {

        // prepare
        val overlay = OverlayHelper.default

        // test
        val json = Json.toJson(overlay)

        // verify
        (json \ "ID").as[Long] mustEqual overlay.ID
        (json \ "StartOffset").asOpt[String] mustEqual overlay.StartOffset
        (json \ "EndOffset").asOpt[String] mustEqual overlay.EndOffset
        (json \ "Layer").as[Int] mustEqual overlay.Layer

      }

    }

    "convert minimum object to Json" in {
      running(FakeApplication()) {

        // prepare
        val overlay = OverlayHelper.defaultMinimum

        // test
        val json = Json.toJson(overlay)

        // verify
        (json \ "ID").as[Long] mustEqual overlay.ID
        (json \ "StartOffset").asOpt[String] mustEqual None
        (json \ "EndOffset").asOpt[String] mustEqual None
        (json \ "Layer").as[Int] mustEqual overlay.Layer

      }

    }

    "convert JSON to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(OverlayHelper.defaultJson)

        // test
        val overlay = json.validate[Overlay]

        // verify
        overlay.get.ID mustEqual (json \ "ID").as[Long]
        overlay.get.StartOffset mustEqual (json \ "StartOffset").asOpt[String]
        overlay.get.EndOffset mustEqual (json \ "EndOffset").asOpt[String]
        overlay.get.Layer mustEqual (json \ "Layer").as[Int]

      }
    }

    "convert minimum JSON to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(OverlayHelper.defaultJsonMinimum)

        // test
        val overlay = json.validate[Overlay]

        // verify
        overlay.get.ID mustEqual (json \ "ID").as[Long]
        overlay.get.StartOffset mustEqual None
        overlay.get.EndOffset mustEqual None
        overlay.get.Layer mustEqual (json \ "Layer").as[Int]

      }
    }

  }

}
