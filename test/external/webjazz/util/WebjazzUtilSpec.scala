package external.webjazz.util

import models.rest.Thumbnail
import models.vimeo.video.Size
import org.specs2.mutable.Specification
import play.api.test.PlayRunners

/**
  * author: cvandrei
  * since: 2016-02-03
  */
class WebjazzUtilSpec extends Specification with PlayRunners {

  "WebjazzUtil." should {

    "sizeToThumbnail() creates Thumbnail based on Size" in {

      // prepare
      val size = Size(20, 40, "link")

      // test
      val thumbnail = WebjazzUtil.sizeToThumbnail(size)

      // verify
      thumbnail.width mustEqual size.width
      thumbnail.height mustEqual size.height
      thumbnail.url mustEqual size.link

    }

    "sizeListToThumbnails() called with empty list" in {

      // prepare
      val l = List.empty[Size]

      // test
      val thumbnails = WebjazzUtil.sizeListToThumbnails(l)

      // verify
      thumbnails.isEmpty must beTrue

    }

    "sizeListToThumbnails() called with non-empty list" in {

      // prepare
      val size1 = Size(10, 20, "link1")
      val size2 = Size(30, 40, "link2")
      val l = List(size1, size2)

      // test
      val thumbnails = WebjazzUtil.sizeListToThumbnails(l)

      // verify
      thumbnails must have size 2

      val thumbnail1 = WebjazzUtil.sizeToThumbnail(size1)
      thumbnails contains thumbnail1

      val thumbnail2 = WebjazzUtil.sizeToThumbnail(size2)
      thumbnails contains thumbnail2

    }

    "createWebjazzNotification() creates WebjazzNotification" in {

      // prepare
      val webjazzToken = "auth-token"
      val vimeoId = -1L
      val hmsId = -2L
      val width = 100
      val height = 200
      val size1 = Size(10, 20, "link1")
      val size2 = Size(30, 40, "link2")
      val sizeList = List(size1, size2)

      // test
      val webjazzNotification = WebjazzUtil.createWebjazzNotification(webjazzToken, vimeoId, hmsId, width, height, sizeList)

      // verify
      (webjazzNotification \ "auth").as[String] mustEqual webjazzToken
      (webjazzNotification \ "vimeo-id").as[Long] mustEqual vimeoId
      (webjazzNotification \ "hms-id").as[Long] mustEqual hmsId
      (webjazzNotification \ "width").as[Int] mustEqual width
      (webjazzNotification \ "height").as[Int] mustEqual height

      val thumbnailList = WebjazzUtil.sizeListToThumbnails(sizeList)
      (webjazzNotification \ "thumbnails").as[List[Thumbnail]] mustEqual thumbnailList

    }

  }

}
