package models.vimeo.video

import models.vimeo.video.util.SizeHelper
import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-01
  */
class SizeSpec extends Specification with PlayRunners {

  "The Json library" should {

    "write object to JSON" in {
      running(FakeApplication()) {

        // prepare
        val size = SizeHelper.defaultSize

        // test
        val json = Json.toJson(size)

        // verify
        val widthCurrent = (json \ "width").as[Int]
        widthCurrent mustEqual SizeHelper.width

        val heightCurrent = (json \ "height").as[Int]
        heightCurrent mustEqual SizeHelper.height

        val linkCurrent = (json \ "link").as[String]
        linkCurrent mustEqual SizeHelper.link

      }
    }

    "parse JSON to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(SizeHelper.defaultJson)

        // test
        val size = json.validate[Size]

        // verify
        size.get.width mustEqual SizeHelper.width
        size.get.height mustEqual SizeHelper.height
        size.get.link mustEqual SizeHelper.link

      }
    }

  }

}
