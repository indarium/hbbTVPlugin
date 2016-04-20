package models

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

  def defaultMinimumObject(channelId: String, stationId: String, showId: Long): Show = {

    Show(None, stationId, stationName, stationLogoUrl, stationLogoDisplay, stationMainColor, channelId,
      channelName, showId, showTitle, showSourceTitle, showSubtitle, showLogoUrl, None, sourceVideoUrl,
      channelBroadcastInfo, rootPortalUrl, None, None)

  }

  def defaultMinimumObject(channelId: String, stationId: String, showId: Long, sdUrl: String): Show = {

    Show(None, stationId, stationName, stationLogoUrl, stationLogoDisplay, stationMainColor, channelId,
      channelName, showId, showTitle, showSourceTitle, showSubtitle, showLogoUrl, None, sdUrl, channelBroadcastInfo,
      rootPortalUrl, None, None)

  }

}
