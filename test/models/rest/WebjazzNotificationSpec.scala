package models.rest

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-16
  */
class WebjazzNotificationSpec extends Specification with PlayRunners {

  "Json library" should {

    "convert object to JSON" in {
      running(FakeApplication()) {

        // prepare
        val auth = "auth"
        val vimeoId = -1L
        val hmsId = -2L
        val width = 100
        val height = 200
        val thumbnail1 = Thumbnail(110, 210, "url-thumb1")
        val thumbnail2 = Thumbnail(120, 220, "url-thumb2")
        val thumbnails = List(thumbnail1, thumbnail2)

        val notification = WebjazzNotification(auth, vimeoId, hmsId, width, height, thumbnails)

        // test
        val json = Json.toJson(notification)

        // verify
        val authCurrent = (json \ "auth").as[String]
        authCurrent mustEqual auth

        val vimeoIdCurrent = (json \ "vimeo-id").as[Long]
        vimeoIdCurrent mustEqual vimeoId

        val hmsIdCurrent = (json \ "hms-id").as[Long]
        hmsIdCurrent mustEqual hmsId

        val widthCurrent = (json \ "width").as[Int]
        widthCurrent mustEqual width

        val heightCurrent = (json \ "height").as[Int]
        heightCurrent mustEqual height

        val thumbnailsCurrent = (json \ "thumbnails").as[List[Thumbnail]]
        thumbnailsCurrent mustEqual thumbnails

      }

    }

  }

}
