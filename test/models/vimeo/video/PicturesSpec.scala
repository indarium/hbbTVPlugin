package models.vimeo.video

import models.vimeo.video.util.PicturesHelper
import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-01
  */
class PicturesSpec extends Specification with PlayRunners {

  "The Json library" should {

    "convert object to JSON" in {
      running(FakeApplication()) {

        // prepare
        val pictures = PicturesHelper.defaultPictures

        // test
        val json = Json.toJson(pictures)

        // verify
        val uriCurrent = (json \ "uri").as[String]
        uriCurrent mustEqual PicturesHelper.uri

        val activeCurrent = (json \ "active").as[Boolean]
        activeCurrent mustEqual PicturesHelper.active

        val picturesTypeCurrent = (json \ "type").as[String]
        picturesTypeCurrent mustEqual PicturesHelper.picturesType

        val sizes = (json \ "sizes").as[List[Size]]
        sizes must have size 2
      }
    }

    "convert JSON to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(PicturesHelper.defaultJson)

        // test
        val pictures = json.validate[Pictures]

        // verify
        pictures.get.uri mustEqual PicturesHelper.uri
        pictures.get.active mustEqual PicturesHelper.active
        pictures.get.pictureType mustEqual PicturesHelper.picturesType
        pictures.get.sizes must have size 2

      }
    }

  }

}
