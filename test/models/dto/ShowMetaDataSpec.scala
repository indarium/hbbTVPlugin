package models.dto

import constants.VimeoEncodingStatusSystem.VimeoEncodingStatus
import models.dto.util.ShowMetaDataHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{FakeApplication, PlayRunners}

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

      }
    }

  }

}
