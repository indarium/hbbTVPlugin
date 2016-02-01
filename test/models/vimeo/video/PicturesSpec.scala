package models.vimeo.video

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-01
  */
class PicturesSpec extends Specification with PlayRunners {

  val vimeoId = 152690945L
  val pictureId = 552752804L
  val uri = s"/videos/$vimeoId/pictures/$pictureId"
  val active = true
  val picturesType = "custom"
  val width1 = 100
  val height1 = 75
  val link1 = s"https://i.vimeocdn.com/video/${pictureId}_${width1}x$height1.jpg?r=pad"
  val width2 = 1280
  val height2 = 720
  val link2 = s"https://i.vimeocdn.com/video/${pictureId}_${width2}x$height2.jpg?r=pad"

  "The Json library" should {

    "write object to JSON" in {
      running(FakeApplication()) {

        // prepare
        val pictures = defaultPictures

        // test
        val json = Json.toJson(pictures)

        // verify
        val uriCurrent = (json \ "uri").as[String]
        uriCurrent mustEqual uri

        val activeCurrent = (json \ "active").as[Boolean]
        activeCurrent mustEqual active

        val picturesTypeCurrent = (json \ "type").as[String]
        picturesTypeCurrent mustEqual picturesType

        // TODO verify picture sizes

      }
    }

    // TODO adapt test
    //    "parse JSON to object" in {
    //      running(FakeApplication()) {
    //
    //        // prepare
    //        val json: JsValue = Json.parse(defaultJson)
    //
    //        // test
    //        val pictures = json.validate[Pictures]
    //
    //        // verify
    //        size.get.width mustEqual width
    //        size.get.height mustEqual height
    //        size.get.link mustEqual link
    //
    //      }
    //    }

  }

  def defaultPictures: Pictures = {
    Pictures(uri, active, picturesType, Seq())
  }

  def defaultJson: String = {

    s"""{
        |"pictures": {
        |    "uri": "$uri",
        |    "active": $active,
        |    "type": "$picturesType",
        |    "sizes": [
        |        {
        |            "width": $width1,
        |            "height": $height1,
        |            "link": "$link1"
        |        },
        |        {
        |            "width": $width2,
        |            "height": $width2,
        |            "link": "$link2"
        |        }
        |    ]
        |}
        |}""".stripMargin

  }

}
