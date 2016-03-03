package models.dto

import java.io.File
import java.net.URL

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

    "convert object to json" in {
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
        (json \ "vimeoEncodingStatus").asOpt[VimeoEncodingStatus] mustEqual meta.vimeoEncodingStatus

      }
    }

  }

}
