package models.vimeo.video

import models.vimeo.video.util.DownloadHelper
import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeApplication, PlayRunners}

/**
  * author: cvandrei
  * since: 2016-02-01
  */
class DownloadSpec extends Specification with PlayRunners {

  "The Json library" should {

    "convert object to JSON" in {
      running(FakeApplication()) {

        // prepare
        val download = DownloadHelper.defaultDownload

        // test
        val json = Json.toJson(download)

        // verify
        val qualityCurrent = (json \ "quality").as[String]
        qualityCurrent mustEqual DownloadHelper.quality

        val fileTypeCurrent = (json \ "type").as[String]
        fileTypeCurrent mustEqual DownloadHelper.fileType

        val widthCurrent = (json \ "width").as[Int]
        widthCurrent mustEqual DownloadHelper.width

        val heightCurrent = (json \ "height").as[Int]
        heightCurrent mustEqual DownloadHelper.height

        val expiresCurrent = (json \ "expires").as[String]
        expiresCurrent mustEqual DownloadHelper.expires

        val linkCurrent = (json \ "link").as[String]
        linkCurrent mustEqual DownloadHelper.link

        val createdTimeCurrent = (json \ "created_time").as[String]
        createdTimeCurrent mustEqual DownloadHelper.createdTime

        val fpsCurrent = (json \ "fps").as[Int]
        fpsCurrent mustEqual DownloadHelper.fps

        val sizeCurrent = (json \ "size").as[Long]
        sizeCurrent mustEqual DownloadHelper.size

        val md5Current = (json \ "md5").as[String]
        md5Current mustEqual DownloadHelper.md5

      }
    }

    "convert JSON to object" in {
      running(FakeApplication()) {

        // prepare
        val json: JsValue = Json.parse(DownloadHelper.defaultJson)

        // test
        val download = json.validate[Download]

        // verify
        download.get.quality mustEqual DownloadHelper.quality
        download.get.fileType mustEqual DownloadHelper.fileType
        download.get.width mustEqual DownloadHelper.width
        download.get.height mustEqual DownloadHelper.height
        download.get.expires mustEqual DownloadHelper.expires
        download.get.link mustEqual DownloadHelper.link
        download.get.createdTime mustEqual DownloadHelper.createdTime
        download.get.fps mustEqual DownloadHelper.fps
        download.get.size mustEqual DownloadHelper.size
        download.get.md5 mustEqual DownloadHelper.md5

      }
    }

  }

}
