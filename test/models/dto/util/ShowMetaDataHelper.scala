package models.dto.util

import java.io.File
import java.net.URL

import constants.VimeoEncodingStatusSystem.IN_PROGRESS
import models.dto.ShowMetaData

/**
  * author: cvandrei
  * since: 2016-02-19
  */
object ShowMetaDataHelper {

  val stationId = "MV1"
  val channelId = "SAT"
  val hmsStationId = "hmsStationId"
  val stationName = "stationName"
  val stationLogoUrl = "http://station.com/logo.png"
  val stationLogoShow = false
  val stationMainColor = "red"
  val channelName = "channelName-SAT"
  val showId = -123L
  val showTitle = s"showTitle--$showId"
  val showSubtitle = s"showSubtitle--$showId"
  val showSourceTitle = s"showSourceTitle--$showId"
  val showLogoUrl = s"http://station.com/show/logo--$showId.png"
  val showLength = 124L
  val showEndInfo = s"showEndInfo--$showId"
  val rootPortalUrl = "http://station.com"
  val isHD = true
  val sourceFilename = s"sourceFilename--$showId"
  val sourceVideoUrl = s"http://station.com/show/$showId-source.mp4"
  val localVideoFile = s"/Users/cvandrei/git/hbbTVPlugin/SAT/MV1/$showId.mp4"
  val publicVideoUrl = s"http://station.com/show/$showId-public.mp4"
  val accessToken = "accessToken"
  val vimeo = true
  val vimeoDone = false
  val vimeoId = -1000L
  val vimeoEncodingStatus = IN_PROGRESS

  def defaultObject(channelId: String, stationId: String, showId: Long): ShowMetaData = {

    val meta = ShowMetaData(stationId, channelId)

    meta.hmsStationId = Some(hmsStationId)
    meta.stationName = Some(stationName)
    meta.stationLogoUrl = Some(new URL(stationLogoUrl))
    meta.stationLogoShow = stationLogoShow
    meta.stationMainColor = Some(stationMainColor)

    meta.channelName = Some(s"$channelName-$channelId")
    meta.showTitle = Some(s"$showTitle-$showId")
    meta.showId = Some(showId)
    meta.showSubtitle = Some(s"$showSubtitle-$showId")
    meta.showSourceTitle = Some(showSourceTitle)
    meta.showLogoUrl = Some(new URL(showLogoUrl))
    meta.showLength = showLength
    meta.showEndInfo = Some(showEndInfo)
    meta.rootPortalUrl = Some(new URL(rootPortalUrl))

    meta.isHD = isHD
    meta.sourceFilename = Some(sourceFilename)
    meta.sourceVideoUrl = Some(new URL(sourceVideoUrl))
    meta.localVideoFile = Some(new File(localVideoFile))
    meta.publicVideoUrl = Some(new URL(publicVideoUrl))

    meta.currentAccessToken = Some(accessToken)

    meta.vimeo = Some(vimeo)
    meta.vimeoDone = Some(vimeoDone)
    meta.vimeoId = Some(vimeoId)
    meta.vimeoEncodingStatus = Some(vimeoEncodingStatus)

    meta

  }

  def defaultMinimumObject(channelId: String, stationId: String): ShowMetaData = {

    val meta = ShowMetaData(stationId, channelId)

    meta.stationLogoShow = stationLogoShow
    meta.showLength = showLength
    meta.isHD = isHD

    meta

  }

  def defaultJson: String = {

    s"""{
        |  "stationId": "$stationId",
        |  "channelId": "$channelId",
        |  "hmsStationId": "$hmsStationId",
        |  "stationName": "$stationName",
        |  "stationLogoUrl": "$stationLogoUrl",
        |  "stationLogoShow": $stationLogoShow,
        |  "stationMainColor": "$stationMainColor",
        |  "channelName": "$channelName",
        |  "showTitle": "$showTitle",
        |  "showId": $showId,
        |  "showSubtitle": "$showSubtitle",
        |  "showSourceTitle": "$showSourceTitle",
        |  "showLogoUrl": "$showLogoUrl",
        |  "showLength": $showLength,
        |  "showEndInfo": "$showEndInfo",
        |  "rootPortalUrl": "$rootPortalUrl",
        |  "isHD": $isHD,
        |  "sourceFilename": "$sourceFilename",
        |  "sourceVideoUrl": "$sourceVideoUrl",
        |  "localVideoFile": "$localVideoFile",
        |  "publicVideoUrl": "$publicVideoUrl",
        |  "currentAccessToken": "$accessToken",
        |  "vimeo": $vimeo,
        |  "vimeoDone": $vimeoDone,
        |  "vimeoId": $vimeoId,
        |  "vimeoEncodingStatus": "${vimeoEncodingStatus.name}"
        |}""".stripMargin

  }

  def defaultMinimumJson: String = {

    s"""{
        |  "stationId": "$stationId",
        |  "channelId": "$channelId",
        |  "stationLogoShow": $stationLogoShow,
        |  "showLength": $showLength,
        |  "isHD": $isHD
        |}""".stripMargin

  }

}
