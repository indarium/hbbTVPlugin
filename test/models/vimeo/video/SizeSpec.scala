package models.vimeo.video

import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-01
  */
class SizeSpec extends Specification with PlayRunners {

  val width = 1280
  val height = 720
  val link = "https://i.vimeocdn.com/video/552752804_1280x720.jpg?r=pad"

  "The Json library" should {

    "write object to JSON" in {
      running(FakeApplication()) {

        // prepare
        val size = defaultSize

        // test
        val json = Json.toJson(size)

        // verify
        val widthCurrent = (json \ "width").as[Int]
        widthCurrent mustEqual width

        val heightCurrent = (json \ "height").as[Int]
        heightCurrent mustEqual height

        val linkCurrent = (json \ "link").as[String]
        linkCurrent mustEqual link

      }
    }

    "parse JSON to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(defaultJson)

        // test
        val size = json.validate[Size]

        // verify
        size.get.width mustEqual width
        size.get.height mustEqual height
        size.get.link mustEqual link

      }
    }

  }

  def defaultSize: Size = {
    Size(width, height, link)
  }

  def defaultJson: String = {

    s"""{
        |    "width": $width,
        |    "height": $height,
        |    "link": "$link"
        |}""".stripMargin

  }

}
