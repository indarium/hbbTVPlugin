package models.vimeo.video

import models.vimeo.video.util.FileHelper
import org.specs2.mutable.Specification
import play.api.libs.json._
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-01
  */
class FileSpec extends Specification with PlayRunners {

  "The Json library" should {

    "write object to JSON" in {
      running(FakeApplication()) {

        // prepare
        val file = FileHelper.defaultFile

        // test
        val json = Json.toJson(file)

        // verify
        val qualityCurrent = (json \ "quality").as[String]
        qualityCurrent mustEqual FileHelper.quality

        val fileTypeCurrent = (json \ "type").as[String]
        fileTypeCurrent mustEqual FileHelper.fileType

        val widthCurrent = (json \ "width").as[Int]
        widthCurrent mustEqual FileHelper.width

        val heightCurrent = (json \ "height").as[Int]
        heightCurrent mustEqual FileHelper.height

        val linkCurrent = (json \ "link").as[String]
        linkCurrent mustEqual FileHelper.link

        val linkSecureCurrent = (json \ "link_secure").as[String]
        linkSecureCurrent mustEqual FileHelper.linkSecure

        val createdTimeCurrent = (json \ "created_time").as[String]
        createdTimeCurrent mustEqual FileHelper.createdTime

        val fpsCurrent = (json \ "fps").as[Int]
        fpsCurrent mustEqual FileHelper.fps

        val sizeCurrent = (json \ "size").as[Long]
        sizeCurrent mustEqual FileHelper.size

        val md5Current = (json \ "md5").as[String]
        md5Current mustEqual FileHelper.md5

      }
    }

    "parse JSON to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(FileHelper.defaultJson)

        // test
        val file = json.validate[File]

        // verify
        file.get.quality mustEqual FileHelper.quality
        file.get.fileType mustEqual FileHelper.fileType
        file.get.width mustEqual FileHelper.width
        file.get.height mustEqual FileHelper.height
        file.get.link mustEqual FileHelper.link
        file.get.linkSecure mustEqual FileHelper.linkSecure
        file.get.createdTime mustEqual FileHelper.createdTime
        file.get.fps mustEqual FileHelper.fps
        file.get.size mustEqual FileHelper.size
        file.get.md5 mustEqual FileHelper.md5

      }
    }

  }

}
