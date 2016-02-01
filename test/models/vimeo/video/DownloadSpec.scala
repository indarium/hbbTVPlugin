package models.vimeo.video

import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-01
  */
class DownloadSpec extends Specification with PlayRunners {

  val quality = "hd"
  val fileType = "video/mp4"
  val width = 1280
  val height = 720
  val expires = "2016-01-22T15:13:33+00:00"
  val link = "https://vimeo.com/api/file/download?clip_id=152690945&id=393716837&profile=113&codec=H264&exp=1453475613&sig=db6c87e0c3e2ea7706c39044beffc9f3fe666552"
  val createdTime = "2016-01-22T11:52:48+00:00"
  val fps = 25
  val size = 120692533L
  val md5 = "b2b3412e3d757943f58d661928ff81bc"

  "The Json library" should {

    "write object to JSON" in {
      running(FakeApplication()) {

        // prepare
        val download = defaultDownload

        // test
        val json = Json.toJson(download)

        // verify
        val qualityCurrent = (json \ "quality").as[String]
        qualityCurrent mustEqual quality

        val fileTypeCurrent = (json \ "type").as[String]
        fileTypeCurrent mustEqual fileType

        val widthCurrent = (json \ "width").as[Int]
        widthCurrent mustEqual width

        val heightCurrent = (json \ "height").as[Int]
        heightCurrent mustEqual height

        val expiresCurrent = (json \ "expires").as[String]
        expiresCurrent mustEqual expires

        val linkCurrent = (json \ "link").as[String]
        linkCurrent mustEqual link

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
        val download = json.validate[Download]

        // verify
        download.get.quality mustEqual quality
        download.get.fileType mustEqual fileType
        download.get.width mustEqual width
        download.get.height mustEqual height
        download.get.expires mustEqual expires
        download.get.link mustEqual link
        download.get.createdTime mustEqual createdTime
        download.get.fps mustEqual fps
        download.get.size mustEqual size
        download.get.md5 mustEqual md5

      }
    }

  }

  def defaultDownload: Download = {
    Download(quality, fileType, width, height, expires, link, createdTime, fps, size, md5)
  }

  def defaultJson: String = {

    s"""{
        |    "quality": "$quality",
        |    "type": "$fileType",
        |    "width": $width,
        |    "height": $height,
        |    "expires": "$expires",
        |    "link": "$link",
        |    "created_time": "$createdTime",
        |    "fps": $fps,
        |    "size": $size,
        |    "md5": "$md5"
        |}""".stripMargin

  }

}
