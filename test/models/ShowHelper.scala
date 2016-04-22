package models

import constants.VimeoEncodingStatusSystem
import constants.VimeoEncodingStatusSystem.VimeoEncodingStatus

/**
  * author: cvandrei
  * since: 2016-04-20
  */
object ShowHelper {

  val stationId = "MV1"
  val stationName = "stationName"
  val stationLogoUrl = "http://station.com/logo.png"
  val stationLogoDisplay = true
  val stationMainColor = "red"
  val channelId = "SAT"
  val channelName = "channelName-SAT"
  val showId = -123L
  val showTitle = s"showTitle--$showId"
  val showSourceTitle = s"showSourceTitle--$showId"
  val showSubtitle = s"showSubtitle--$showId"
  val showLogoUrl = s"http://station.com/show/logo--$showId.png"
  val sourceVideoUrl = s"http://station.com/show/$showId-source.mp4"
  val channelBroadcastInfo = s"channelBroadcastInfo--$showId"

  val hmsStationId = "hmsStationId"
  val stationLogoShow = false
  val showLength = 124L
  val showEndInfo = s"showEndInfo--$showId"
  val rootPortalUrl = "http://station.com"
  val isHD = true
  val sourceFilename = s"sourceFilename--$showId"
  val localVideoFile = s"/Users/cvandrei/git/hbbTVPlugin/SAT/MV1/$showId.mp4"
  val publicVideoUrl = s"http://station.com/show/$showId-public.mp4"
  val accessToken = "accessToken"

  // optional fields
  val hdUrl = s"http://station.com/show/$showId-source.mp4"
  val vimeoId = -12341234L
  val vimeoEncodingStatus = VimeoEncodingStatusSystem.IN_PROGRESS
  val s3Name = "SAT/MV1/random-uuid.mp4"


  def defaultMinimumObject(channelId: String, stationId: String, showId: Long): Show = {

    Show(None, stationId, stationName, stationLogoUrl, stationLogoDisplay, stationMainColor, channelId,
      channelName, showId, showTitle, showSourceTitle, showSubtitle, showLogoUrl, None, sourceVideoUrl,
      channelBroadcastInfo, rootPortalUrl, None, None, None)

  }

  def defaultObject(channelId: String, stationId: String, showId: Long): Show = {

    Show(None, stationId, stationName, stationLogoUrl, stationLogoDisplay, stationMainColor, channelId,
      channelName, showId, showTitle, showSourceTitle, showSubtitle, showLogoUrl, Some(hdUrl), sourceVideoUrl,
      channelBroadcastInfo, rootPortalUrl, Some(vimeoId), Some(vimeoEncodingStatus), Some(s3Name))

  }

  def defaultMinimumObject(channelId: String, stationId: String, showId: Long, sdUrl: String): Show = {

    Show(None, stationId, stationName, stationLogoUrl, stationLogoDisplay, stationMainColor, channelId,
      channelName, showId, showTitle, showSourceTitle, showSubtitle, showLogoUrl, None, sdUrl, channelBroadcastInfo,
      rootPortalUrl, None, None, None)

  }

  def withVideoUrlsAndVimeoEncodingStatus(
                                           sdUrl: String,
                                           hdUrl: Option[String],
                                           vimeoEncodingStatus: Option[VimeoEncodingStatus]) = {

    defaultMinimumObject(channelId, stationId, showId)
      .copy(
        showVideoSDUrl = sdUrl,
        showVideoHDUrl = hdUrl,
        vimeoEncodingStatus = vimeoEncodingStatus
      )

  }

}
