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

  def defaultObject(channelId: String, stationId: String, showId: Long): ShowMetaData = {

    val meta = ShowMetaData(stationId, channelId)

    meta.hmsStationId = Some("hmsStationId")
    meta.stationName = Some("stationName")
    meta.stationLogoUrl = Some(new URL("http://station.com/logo.png"))
    meta.stationLogoShow = false
    meta.stationMainColor = Some("red")

    meta.channelName = Some(s"channelName-$channelId")
    meta.showTitle = Some(s"showTitle-$showId")
    meta.showId = Some(showId)
    meta.showSubtitle = Some(s"showSubTitle-$showId")
    meta.showSourceTitle = Some(s"showSourceTitle-$showId")
    meta.showLogoUrl = Some(new URL(s"http://station.com/show/logo-$showId.png"))
    meta.showLength = 124L
    meta.showEndInfo = Some(s"showEndInfo-$showId")
    meta.rootPortalUrl = Some(new URL("http://station.com"))

    meta.isHD = true
    meta.sourceFilename = Some(s"sourceFilename-$showId")
    meta.sourceVideoUrl = Some(new URL(s"http://station.com/show/$showId.mp4"))
    meta.localVideoFile = Some(new File("$HOME" + s"/$channelId/$stationId/$showId.mp4"))
    meta.sourceVideoUrl = Some(new URL(s"http://station.com/show/$showId.mp4"))

    meta.currentAccessToken = Some("accessToken")

    meta.vimeo = Some(true)
    meta.vimeoDone = Some(false)
    meta.vimeoId = Some(-1000L)
    meta.vimeoEncodingStatus = Some(IN_PROGRESS)

    meta

  }

}
