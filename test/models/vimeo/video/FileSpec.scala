package models.vimeo.video

import org.specs2.mutable.Specification
import play.api.libs.json._
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-01
  */
class FileSpec extends Specification with PlayRunners {

  val quality = "hd"
  val fileType = "video/mp4"
  val width = 1280
  val height = 720
  val link = "http://player.vimeo.com/external/152690945.hd.mp4?s=e514c83b1988801c9067e150d2470e32bfc1c2c0&profile_id=113&oauth2_token_id=393716837"
  val linkSecure = "https://player.vimeo.com/external/152690945.hd.mp4?s=e514c83b1988801c9067e150d2470e32bfc1c2c0&profile_id=113&oauth2_token_id=393716837"
  val createdTime = "2016-01-22T11:52:48+00:00"
  val fps = 25
  val size = 120692533L
  val md5 = "b2b3412e3d757943f58d661928ff81bc"

  "The Json library" should {

    "write object to JSON" in {
      running(FakeApplication()) {

        // prepare
        val file = defaultFile

        // test
        val json = Json.toJson(file)

        // verify
        val qualityCurrent = (json \ "quality").as[String]
        qualityCurrent mustEqual quality

        val fileTypeCurrent = (json \ "type").as[String]
        fileTypeCurrent mustEqual fileType

        val widthCurrent = (json \ "width").as[Int]
        widthCurrent mustEqual width

        val heightCurrent = (json \ "height").as[Int]
        heightCurrent mustEqual height

        val linkCurrent = (json \ "link").as[String]
        linkCurrent mustEqual link

        val linkSecureCurrent = (json \ "link_secure").as[String]
        linkSecureCurrent mustEqual linkSecure

        val createdTimeCurrent = (json \ "created_time").as[String]
        createdTimeCurrent mustEqual createdTime

        val fpsCurrent = (json \ "fps").as[Int]
        fpsCurrent mustEqual fps

        val sizeCurrent = (json \ "size").as[Long]
        sizeCurrent mustEqual size

        val md5Current = (json \ "md5").as[String]
        md5Current mustEqual md5

      }
    }

    "parse JSON to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(defaultJson)

        // test
        val file = json.validate[File]

        // verify
        file.get.quality mustEqual quality
        file.get.fileType mustEqual fileType
        file.get.width mustEqual width
        file.get.height mustEqual height
        file.get.link mustEqual link
        file.get.linkSecure mustEqual linkSecure
        file.get.createdTime mustEqual createdTime
        file.get.fps mustEqual fps
        file.get.size mustEqual size
        file.get.md5 mustEqual md5

      }
    }

  }

  def defaultFile: File = {
    File(quality, fileType, width, height, link, linkSecure, createdTime, fps, size, md5)
  }

  def defaultJson: String = {

    s"""{
        |    "quality": "$quality",
        |    "type": "$fileType",
        |    "width": $width,
        |    "height": $height,
        |    "link": "$link",
        |    "link_secure": "$linkSecure",
        |    "created_time": "$createdTime",
        |    "fps": $fps,
        |    "size": $size,
        |    "md5": "$md5"
        |}""".stripMargin

  }

}
