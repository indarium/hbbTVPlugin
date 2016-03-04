package models.dto

import java.io.File
import java.net.URL

import constants.VimeoEncodingStatusSystem.{IN_PROGRESS, VimeoEncodingStatus}
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
        val meta = ShowMetaDataHelper.defaultObject("SAT", "MV1", -123L)
        meta.hmsStationId = None
        meta.stationName = None
        meta.stationMainColor = None
        meta.channelName = None
        meta.showTitle = None
        meta.showId = None
        meta.showSubtitle = None
        meta.showSourceTitle = None
        meta.showEndInfo = None
        meta.sourceFilename = None
        meta.vimeo = None
        meta.currentAccessToken = None
        meta.vimeoDone = None
        meta.vimeoId = None
        meta.vimeoEncodingStatus = None

        // test
        val json = Json.toJson(meta)

        // verify
        (json \ "stationId").as[String] mustEqual meta.stationId
        (json \ "channelId").as[String] mustEqual meta.channelId
        (json \ "hmsStationId").asOpt[String] mustEqual None
        (json \ "stationName").asOpt[String] mustEqual None
        ShowMetaData.parseOptUrl(json, "stationLogoUrl") mustEqual meta.stationLogoUrl
        (json \ "stationLogoShow").as[Boolean] mustEqual meta.stationLogoShow
        (json \ "stationMainColor").asOpt[String] mustEqual None

        (json \ "channelName").asOpt[String] mustEqual None
        (json \ "showTitle").asOpt[String] mustEqual None
        (json \ "showId").asOpt[Long] mustEqual None
        (json \ "showSubtitle").asOpt[String] mustEqual None
        (json \ "showSourceTitle").asOpt[String] mustEqual None
        ShowMetaData.parseOptUrl(json, "showLogoUrl") mustEqual meta.showLogoUrl
        (json \ "showLength").as[Long] mustEqual meta.showLength
        (json \ "showEndInfo").asOpt[String] mustEqual None
        ShowMetaData.parseOptUrl(json, "rootPortalUrl") mustEqual meta.rootPortalUrl

        (json \ "isHD").as[Boolean] mustEqual meta.isHD
        (json \ "sourceFilename").asOpt[String] mustEqual None
        ShowMetaData.parseOptUrl(json, "sourceVideoUrl") mustEqual meta.sourceVideoUrl
        ShowMetaData.parseOptFile(json, "localVideoFile").get.getAbsolutePath mustEqual meta.localVideoFile.get.getAbsolutePath
        ShowMetaData.parseOptUrl(json, "publicVideoUrl") mustEqual meta.publicVideoUrl

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
        val stationId = "MV1"
        val channelId = "SAT"
        val hmsStationId = Some("hmsStationId")
        val stationName = Some("stationName")
        val stationLogoUrl = Some(new URL("http://station.com/logo.png"))
        val stationLogoShow = false
        val stationMainColor = Some("red")
        val channelName = Some("channelName-SAT")
        val showTitle = Some("showTitle--123")
        val showId = Some(-123L)
        val showSubTitle = Some("showSubTitle--123")
        val showSourceTitle = Some("showSourceTitle--123")
        val showLogoUrl = Some(new URL("http://station.com/show/logo--123.png"))
        val showLength = 124L
        val showEndInfo = Some("showEndInfo--123")
        val rootPortalUrl = Some(new URL("http://station.com"))
        val isHd = true
        val sourceFilename = Some("sourceFilename--123")
        val sourceVideoUrl = Some(new URL("http://station.com/show/-123-source.mp4"))
        val localVideoFile = Some(new File("/Users/cvandrei/git/hbbTVPlugin/SAT/MV1/-123.mp4"))
        val publicVideoUrl = Some(new URL("http://station.com/show/-123-public.mp4"))
        val currentAccessToken = Some("accessToken")
        val vimeo = Some(true)
        val vimeoDone = Some(false)
        val vimeoId = Some(-1000L)
        val vimeoEncodingStatus = Some(IN_PROGRESS)

        val json = Json.parse(s"""{
                                |  "stationId": "$stationId",
                                |  "channelId": "$channelId",
                                |  "hmsStationId": "${hmsStationId.get}",
                                |  "stationName": "${stationName.get}",
                                |  "stationLogoUrl": "${stationLogoUrl.get.toString}",
                                |  "stationLogoShow": $stationLogoShow,
                                |  "stationMainColor": "${stationMainColor.get}",
                                |  "channelName": "${channelName.get}",
                                |  "showTitle": "${showTitle.get}",
                                |  "showId": ${showId.get},
                                |  "showSubtitle": "${showSubTitle.get}",
                                |  "showSourceTitle": "${showSourceTitle.get}",
                                |  "showLogoUrl": "${showLogoUrl.get.toString}",
                                |  "showLength": $showLength,
                                |  "showEndInfo": "${showEndInfo.get}",
                                |  "rootPortalUrl": "${rootPortalUrl.get.toString}",
                                |  "isHD": $isHd,
                                |  "sourceFilename": "${sourceFilename.get}",
                                |  "sourceVideoUrl": "${sourceVideoUrl.get.toString}",
                                |  "localVideoFile": "${localVideoFile.get.getAbsolutePath}",
                                |  "publicVideoUrl": "${publicVideoUrl.get.toString}",
                                |  "currentAccessToken": "${currentAccessToken.get}",
                                |  "vimeo": ${vimeo.get},
                                |  "vimeoDone": ${vimeoDone.get},
                                |  "vimeoId": ${vimeoId.get},
                                |  "vimeoEncodingStatus": "${vimeoEncodingStatus.get.name}"
                                |}""".stripMargin)

        // test
        val meta = json.validate[ShowMetaData].get

        // verify
        meta.stationId mustEqual stationId
        meta.channelId mustEqual channelId
        meta.hmsStationId mustEqual hmsStationId
        meta.stationName mustEqual stationName
        meta.stationLogoUrl mustEqual stationLogoUrl
        meta.stationLogoShow mustEqual stationLogoShow
        meta.stationMainColor mustEqual stationMainColor

        meta.channelName mustEqual channelName
        meta.showTitle mustEqual showTitle
        meta.showId mustEqual showId
        meta.showSubtitle mustEqual showSubTitle
        meta.showSourceTitle mustEqual showSourceTitle
        meta.showLogoUrl mustEqual showLogoUrl
        meta.showLength mustEqual showLength
        meta.showEndInfo mustEqual showEndInfo
        meta.rootPortalUrl mustEqual rootPortalUrl

        meta.isHD mustEqual isHd
        meta.sourceFilename mustEqual sourceFilename
        meta.sourceVideoUrl mustEqual sourceVideoUrl
        meta.localVideoFile mustEqual localVideoFile
        meta.publicVideoUrl mustEqual publicVideoUrl

        meta.currentAccessToken mustEqual currentAccessToken

        meta.vimeo mustEqual vimeo
        meta.vimeoDone mustEqual vimeoDone
        meta.vimeoId mustEqual vimeoId
        meta.vimeoEncodingStatus mustEqual vimeoEncodingStatus

      }
    }

    "convert json (only mandatory fields set) to object" in {
      running(FakeApplication()) {

        // prepare
        val stationId = "MV1"
        val channelId = "SAT"
        val stationLogoShow = false
        val showLength = 124L
        val isHd = true

        val json = Json.parse(s"""{
                                |  "stationId": "$stationId",
                                |  "channelId": "$channelId",
                                |  "stationLogoShow": $stationLogoShow,
                                |  "showLength": $showLength,
                                |  "isHD": $isHd
                                |}""".stripMargin)

        // test
        val meta = json.validate[ShowMetaData].get

        // verify
        meta.stationId mustEqual stationId
        meta.channelId mustEqual channelId
        meta.hmsStationId mustEqual None
        meta.stationName mustEqual None
        meta.stationLogoUrl mustEqual None
        meta.stationLogoShow mustEqual stationLogoShow
        meta.stationMainColor mustEqual None

        meta.channelName mustEqual None
        meta.showTitle mustEqual None
        meta.showId mustEqual None
        meta.showSubtitle mustEqual None
        meta.showSourceTitle mustEqual None
        meta.showLogoUrl mustEqual None
        meta.showLength mustEqual showLength
        meta.showEndInfo mustEqual None
        meta.rootPortalUrl mustEqual None

        meta.isHD mustEqual isHd
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
