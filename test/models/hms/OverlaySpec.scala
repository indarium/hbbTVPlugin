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
        (json \ "StartOffset").as[String] mustEqual overlay.StartOffset
        (json \ "EndOffset").as[String] mustEqual overlay.EndOffset
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
        overlay.get.StartOffset mustEqual (json \ "StartOffset").as[String]
        overlay.get.EndOffset mustEqual (json \ "EndOffset").as[String]
        overlay.get.Layer mustEqual (json \ "Layer").as[Int]

      }
    }

    "foo" in {
      running(FakeApplication()) {
        val list = OverlayHelper.defaultList
        val json = Json.toJson(list).toString()

        json mustEqual ""
      }
    }

  }

}
