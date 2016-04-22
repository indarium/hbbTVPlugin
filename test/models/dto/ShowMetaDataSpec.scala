package models.dto

import constants.VimeoEncodingStatusSystem.VimeoEncodingStatus
import models.dto.util.ShowMetaDataHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{FakeApplication, PlayRunners}
import reactivemongo.bson.BSON

/**
  * author: cvandrei
  * since: 2016-02-19
  */
class ShowMetaDataSpec extends Specification with PlayRunners {

  "json library" should {

    "convert object (all fields set) to json" in {
      running(FakeApplication()) {

        // prepare
        val meta = ShowMetaDataHelper.defaultObject("SAT", "MV1", -123L)

        // test
        val json = Json.toJson(meta)

        // verify
        (json \ "stationId").as[String] mustEqual meta.stationId
        (json \ "channelId").as[String] mustEqual meta.channelId
        (json \ "hmsStationId").asOpt[String] mustEqual meta.hmsStationId
        (json \ "stationName").asOpt[String] mustEqual meta.stationName
        ShowMetaData.parseOptUrl(json, "stationLogoUrl") mustEqual meta.stationLogoUrl
        (json \ "stationLogoShow").as[Boolean] mustEqual meta.stationLogoShow
        (json \ "stationMainColor").asOpt[String] mustEqual meta.stationMainColor

        (json \ "channelName").asOpt[String] mustEqual meta.channelName
        (json \ "showTitle").asOpt[String] mustEqual meta.showTitle
        (json \ "showId").asOpt[Long] mustEqual meta.showId
        (json \ "showSubtitle").asOpt[String] mustEqual meta.showSubtitle
        (json \ "showSourceTitle").asOpt[String] mustEqual meta.showSourceTitle
        ShowMetaData.parseOptUrl(json, "showLogoUrl") mustEqual meta.showLogoUrl
        (json \ "showLength").as[Long] mustEqual meta.showLength
        (json \ "showEndInfo").asOpt[String] mustEqual meta.showEndInfo
        ShowMetaData.parseOptUrl(json, "rootPortalUrl") mustEqual meta.rootPortalUrl

        (json \ "isHD").as[Boolean] mustEqual meta.isHD
        (json \ "sourceFilename").asOpt[String] mustEqual meta.sourceFilename
        ShowMetaData.parseOptUrl(json, "sourceVideoUrl") mustEqual meta.sourceVideoUrl
        ShowMetaData.parseOptFile(json, "localVideoFile").get.getAbsolutePath mustEqual meta.localVideoFile.get.getAbsolutePath
        ShowMetaData.parseOptUrl(json, "publicVideoUrl") mustEqual meta.publicVideoUrl

        (json \ "currentAccessToken").asOpt[String] mustEqual meta.currentAccessToken

        (json \ "vimeo").asOpt[Boolean] mustEqual meta.vimeo
        (json \ "vimeoDone").asOpt[Boolean] mustEqual meta.vimeoDone
        (json \ "vimeoId").asOpt[Long] mustEqual meta.vimeoId
        (json \ "vimeoEncodingStatus").asOpt[String] mustEqual Some(meta.vimeoEncodingStatus.get.name)

        (json \ "s3Name").asOpt[String] mustEqual meta.s3Name

      }
    }

    "convert object (only mandatory fields set) to json" in {
      running(FakeApplication()) {

        // prepare
        val meta = ShowMetaDataHelper.defaultMinimumObject("SAT", "MV1")

        // test
        val json = Json.toJson(meta)

        // verify
        (json \ "stationId").as[String] mustEqual meta.stationId
        (json \ "channelId").as[String] mustEqual meta.channelId
        (json \ "hmsStationId").asOpt[String] mustEqual None
        (json \ "stationName").asOpt[String] mustEqual None
        ShowMetaData.parseOptUrl(json, "stationLogoUrl") mustEqual None
        (json \ "stationLogoShow").as[Boolean] mustEqual meta.stationLogoShow
        (json \ "stationMainColor").asOpt[String] mustEqual None

        (json \ "channelName").asOpt[String] mustEqual None
        (json \ "showTitle").asOpt[String] mustEqual None
        (json \ "showId").asOpt[Long] mustEqual None
        (json \ "showSubtitle").asOpt[String] mustEqual None
        (json \ "showSourceTitle").asOpt[String] mustEqual None
        ShowMetaData.parseOptUrl(json, "showLogoUrl") mustEqual None
        (json \ "showLength").as[Long] mustEqual meta.showLength
        (json \ "showEndInfo").asOpt[String] mustEqual None
        ShowMetaData.parseOptUrl(json, "rootPortalUrl") mustEqual None

        (json \ "isHD").as[Boolean] mustEqual meta.isHD
        (json \ "sourceFilename").asOpt[String] mustEqual None
        ShowMetaData.parseOptUrl(json, "sourceVideoUrl") mustEqual None
        ShowMetaData.parseOptFile(json, "localVideoFile") mustEqual None
        ShowMetaData.parseOptUrl(json, "publicVideoUrl") mustEqual None

        (json \ "currentAccessToken").asOpt[String] mustEqual None

        (json \ "vimeo").asOpt[Boolean] mustEqual None
        (json \ "vimeoDone").asOpt[Boolean] mustEqual None
        (json \ "vimeoId").asOpt[Long] mustEqual None
        (json \ "vimeoEncodingStatus").asOpt[VimeoEncodingStatus] mustEqual None

        (json \ "s3Name").asOpt[String] mustEqual None

      }
    }

    "convert json (all fields set) to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(ShowMetaDataHelper.defaultJson)

        // test
        val meta = json.validate[ShowMetaData].get

        // verify
        meta.stationId mustEqual (json \ "stationId").as[String]
        meta.channelId mustEqual (json \ "channelId").as[String]
        meta.hmsStationId mustEqual (json \ "hmsStationId").asOpt[String]
        meta.stationName mustEqual (json \ "stationName").asOpt[String]
        meta.stationLogoUrl.get.toString mustEqual (json \ "stationLogoUrl").as[String]
        meta.stationLogoShow mustEqual (json \ "stationLogoShow").as[Boolean]
        meta.stationMainColor mustEqual (json \ "stationMainColor").asOpt[String]

        meta.channelName mustEqual (json \ "channelName").asOpt[String]
        meta.showTitle mustEqual (json \ "showTitle").asOpt[String]
        meta.showId mustEqual (json \ "showId").asOpt[Long]
        meta.showSubtitle mustEqual (json \ "showSubtitle").asOpt[String]
        meta.showSourceTitle mustEqual (json \ "showSourceTitle").asOpt[String]
        meta.showLogoUrl.get.toString mustEqual (json \ "showLogoUrl").as[String]
        meta.showLength mustEqual (json \ "showLength").as[Long]
        meta.showEndInfo mustEqual (json \ "showEndInfo").asOpt[String]
        meta.rootPortalUrl.get.toString mustEqual (json \ "rootPortalUrl").as[String]

        meta.isHD mustEqual (json \ "isHD").as[Boolean]
        meta.sourceFilename mustEqual (json \ "sourceFilename").asOpt[String]
        meta.sourceVideoUrl.get.toString mustEqual (json \ "sourceVideoUrl").as[String]
        meta.localVideoFile.get.getAbsolutePath mustEqual (json \ "localVideoFile").as[String]
        meta.publicVideoUrl.get.toString mustEqual (json \ "publicVideoUrl").as[String]

        meta.currentAccessToken mustEqual (json \ "currentAccessToken").asOpt[String]

        meta.vimeo mustEqual (json \ "vimeo").asOpt[Boolean]
        meta.vimeoDone mustEqual (json \ "vimeoDone").asOpt[Boolean]
        meta.vimeoId mustEqual (json \ "vimeoId").asOpt[Long]
        meta.vimeoEncodingStatus.get.name mustEqual (json \ "vimeoEncodingStatus").as[String]

        meta.s3Name mustEqual (json \ "s3Name").asOpt[String]

      }
    }

    "convert json (only mandatory fields set) to object" in {
      running(FakeApplication()) {

        // prepare
        val json = Json.parse(ShowMetaDataHelper.defaultMinimumJson)

        // test
        val meta = json.validate[ShowMetaData].get

        // verify
        meta.stationId mustEqual (json\ "stationId").as[String]
        meta.channelId mustEqual (json\ "channelId").as[String]
        meta.hmsStationId mustEqual None
        meta.stationName mustEqual None
        meta.stationLogoUrl mustEqual None
        meta.stationLogoShow mustEqual (json\ "stationLogoShow").as[Boolean]
        meta.stationMainColor mustEqual None

        meta.channelName mustEqual None
        meta.showTitle mustEqual None
        meta.showId mustEqual None
        meta.showSubtitle mustEqual None
        meta.showSourceTitle mustEqual None
        meta.showLogoUrl mustEqual None
        meta.showLength mustEqual (json\ "showLength").as[Long]
        meta.showEndInfo mustEqual None
        meta.rootPortalUrl mustEqual None

        meta.isHD mustEqual (json \ "isHD").as[Boolean]
        meta.sourceFilename mustEqual None
        meta.sourceVideoUrl mustEqual None
        meta.localVideoFile mustEqual None
        meta.publicVideoUrl mustEqual None

        meta.currentAccessToken mustEqual None

        meta.vimeo mustEqual None
        meta.vimeoDone mustEqual None
        meta.vimeoId mustEqual None
        meta.vimeoEncodingStatus mustEqual None

        meta.s3Name mustEqual None

      }
    }

    "convert object (all fields set) to bson" in {
      running(FakeApplication()) {

        // prepare
        val meta = ShowMetaDataHelper.defaultObject("SAT", "MV1", 5678010794297290347L)

        // test
        val bson = BSON.writeDocument(meta)

        // verify
        bson.getAs[String]("stationId").get mustEqual meta.stationId
        bson.getAs[String]("channelId").get mustEqual meta.channelId
        bson.getAs[String]("hmsStationId") mustEqual meta.hmsStationId
        bson.getAs[String]("stationName") mustEqual meta.stationName
        bson.getAs[String]("stationLogoUrl").get mustEqual meta.stationLogoUrl.get.toString
        bson.getAs[Boolean]("stationLogoShow").get mustEqual meta.stationLogoShow
        bson.getAs[String]("stationMainColor") mustEqual meta.stationMainColor

        bson.getAs[String]("channelName") mustEqual meta.channelName
        bson.getAs[String]("showTitle") mustEqual meta.showTitle
        bson.getAs[Long]("showId") mustEqual meta.showId
        bson.getAs[String]("showSubtitle") mustEqual meta.showSubtitle
        bson.getAs[String]("showSourceTitle") mustEqual meta.showSourceTitle
        bson.getAs[String]("showLogoUrl").get mustEqual meta.showLogoUrl.get.toString
        bson.getAs[Long]("showLength").get mustEqual meta.showLength
        bson.getAs[String]("showEndInfo") mustEqual meta.showEndInfo
        bson.getAs[String]("rootPortalUrl").get mustEqual meta.rootPortalUrl.get.toString

        bson.getAs[Boolean]("isHD").get mustEqual meta.isHD
        bson.getAs[String]("sourceFilename") mustEqual meta.sourceFilename
        bson.getAs[String]("sourceVideoUrl").get mustEqual meta.sourceVideoUrl.get.toString
        bson.getAs[String]("localVideoFile").get mustEqual meta.localVideoFile.get.getAbsolutePath
        bson.getAs[String]("publicVideoUrl").get mustEqual meta.publicVideoUrl.get.toString

        bson.getAs[String]("currentAccessToken") mustEqual meta.currentAccessToken

        bson.getAs[Boolean]("vimeo") mustEqual meta.vimeo
        bson.getAs[Boolean]("vimeoDone") mustEqual meta.vimeoDone
        bson.getAs[Long]("vimeoId") mustEqual meta.vimeoId
        bson.getAs[String]("vimeoEncodingStatus").get mustEqual meta.vimeoEncodingStatus.get.name

        bson.getAs[String]("s3Name") mustEqual meta.s3Name

      }
    }

    "convert object (only mandatory fields set) to bson" in {
      running(FakeApplication()) {

        // prepare
        val meta = ShowMetaDataHelper.defaultMinimumObject("SAT", "MV1")

        // test
        val json = Json.toJson(meta)

        // verify
        val bson = BSON.writeDocument(meta)

        // verify
        bson.getAs[String]("stationId").get mustEqual meta.stationId
        bson.getAs[String]("channelId").get mustEqual meta.channelId
        bson.getAs[String]("hmsStationId").isEmpty must beTrue
        bson.getAs[String]("stationName").isEmpty must beTrue
        bson.getAs[String]("stationLogoUrl").isEmpty must beTrue
        bson.getAs[Boolean]("stationLogoShow").get mustEqual meta.stationLogoShow
        bson.getAs[String]("stationMainColor").isEmpty must beTrue

        bson.getAs[String]("channelName").isEmpty must beTrue
        bson.getAs[String]("showTitle").isEmpty must beTrue
        bson.getAs[Long]("showId").isEmpty must beTrue
        bson.getAs[String]("showSubtitle").isEmpty must beTrue
        bson.getAs[String]("showSourceTitle").isEmpty must beTrue
        bson.getAs[String]("showLogoUrl").isEmpty must beTrue
        bson.getAs[Long]("showLength").get mustEqual meta.showLength
        bson.getAs[String]("showEndInfo").isEmpty must beTrue
        bson.getAs[String]("rootPortalUrl").isEmpty must beTrue

        bson.getAs[Boolean]("isHD").get mustEqual meta.isHD
        bson.getAs[String]("sourceFilename").isEmpty must beTrue
        bson.getAs[String]("sourceVideoUrl").isEmpty must beTrue
        bson.getAs[String]("localVideoFile").isEmpty must beTrue
        bson.getAs[String]("publicVideoUrl").isEmpty must beTrue

        bson.getAs[String]("currentAccessToken").isEmpty must beTrue

        bson.getAs[Boolean]("vimeo").isEmpty must beTrue
        bson.getAs[Boolean]("vimeoDone").isEmpty must beTrue
        bson.getAs[Long]("vimeoId").isEmpty must beTrue
        bson.getAs[String]("vimeoEncodingStatus").isEmpty must beTrue

        bson.getAs[String]("s3Name").isEmpty must beTrue

      }
    }

    "convert bson (all fields set) to object" in {
      running(FakeApplication()) {

        // prepare
        val meta = ShowMetaDataHelper.defaultObject("SAT", "MV1", 5678010794297290347L)
        val bson = BSON.write(meta)

        // test
        val o = BSON.readDocument[ShowMetaData](bson)

        // verify
        o.stationId mustEqual meta.stationId
        o.channelId mustEqual meta.channelId
        o.hmsStationId mustEqual meta.hmsStationId
        o.stationName mustEqual meta.stationName
        o.stationLogoUrl mustEqual meta.stationLogoUrl
        o.stationLogoShow mustEqual meta.stationLogoShow
        o.stationMainColor mustEqual meta.stationMainColor

        o.channelName mustEqual meta.channelName
        o.showTitle mustEqual meta.showTitle
        o.showId mustEqual meta.showId
        o.showSubtitle mustEqual meta.showSubtitle
        o.showSourceTitle mustEqual meta.showSourceTitle
        o.showLogoUrl mustEqual meta.showLogoUrl
        o.showLength mustEqual meta.showLength
        o.showEndInfo mustEqual meta.showEndInfo
        o.rootPortalUrl mustEqual meta.rootPortalUrl

        o.isHD mustEqual meta.isHD
        o.sourceFilename mustEqual meta.sourceFilename
        o.sourceVideoUrl mustEqual meta.sourceVideoUrl
        o.localVideoFile mustEqual meta.localVideoFile
        o.publicVideoUrl mustEqual meta.publicVideoUrl

        o.currentAccessToken mustEqual meta.currentAccessToken

        o.vimeo mustEqual meta.vimeo
        o.vimeoDone mustEqual meta.vimeoDone
        o.vimeoId mustEqual meta.vimeoId
        o.vimeoEncodingStatus mustEqual meta.vimeoEncodingStatus

        o.s3Name mustEqual meta.s3Name

      }
    }

    "convert bson (only mandatory fields set) to bson" in {
      running(FakeApplication()) {

        // prepare
        val meta = ShowMetaDataHelper.defaultMinimumObject("SAT", "MV1")
        val bson = BSON.write(meta)

        // test
        val o = BSON.readDocument[ShowMetaData](bson)

        // verify
        o.stationId mustEqual meta.stationId
        o.channelId mustEqual meta.channelId
        o.hmsStationId.isEmpty must beTrue
        o.stationName.isEmpty must beTrue
        o.stationLogoUrl.isEmpty must beTrue
        o.stationLogoShow mustEqual meta.stationLogoShow
        o.stationMainColor.isEmpty must beTrue

        o.channelName.isEmpty must beTrue
        o.showTitle.isEmpty must beTrue
        o.showId.isEmpty must beTrue
        o.showSubtitle.isEmpty must beTrue
        o.showSourceTitle.isEmpty must beTrue
        o.showLogoUrl.isEmpty must beTrue
        o.showLength mustEqual meta.showLength
        o.showEndInfo.isEmpty must beTrue
        o.rootPortalUrl.isEmpty must beTrue

        o.isHD mustEqual meta.isHD
        o.sourceFilename.isEmpty must beTrue
        o.sourceVideoUrl.isEmpty must beTrue
        o.localVideoFile.isEmpty must beTrue
        o.publicVideoUrl.isEmpty must beTrue

        o.currentAccessToken.isEmpty must beTrue

        o.vimeo.isEmpty must beTrue
        o.vimeoDone.isEmpty must beTrue
        o.vimeoId.isEmpty must beTrue
        o.vimeoEncodingStatus.isEmpty must beTrue

        o.s3Name.isEmpty must beTrue

      }
    }

  }

}
