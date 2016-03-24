package helper.hms

import helper.Config
import models.Station
import models.hms.HmsShow

/**
  * author: cvandrei
  * since: 2016-03-14
  */
object HmsUtil {

  def getShowsUrl(stationOpt: Option[Station]): Option[String] = {

    stationOpt match {

      case None => None

      case Some(station) =>

        val baseUrl = Config.hmsBroadcastUrl
        val channelId = station.channelId
        val encStationID = java.net.URLEncoder.encode(station.stationId, "UTF-8")
        val encHmsStationID = java.net.URLEncoder.encode(station.hmsStationId, "UTF-8")

        val path = station
          .getShowUrlPattern.getOrElse("/Show/{CHANNEL-ID}?Category={HMS-STATION-ID}&Order=DESC&Count=25")
          .replace("{CHANNEL-ID}", channelId)
          .replace("{STATION-ID}", encStationID)
          .replace("{HMS-STATION-ID}", encHmsStationID)
        val url = baseUrl + path

        Some(url)

    }

  }

  def isTranscoderEnabled(stationId: String): Boolean = {

    val stationIdLowerCase = stationId.toLowerCase
    Config.hmsTranscoderActivateGlobal match {
      case true => !Config.hmsTranscoderDeactivateChannels.contains(stationIdLowerCase)
      case false => Config.hmsTranscoderActivateChannels.contains(stationIdLowerCase)
    }

  }

  def extractCurrentShow(showSeq: Seq[HmsShow], stationId: String): Option[HmsShow] = {

    showSeq.find {

      show =>

        HmsUtil.isTranscoderEnabled(stationId) match {
          case true => show.UTCEnd.isBeforeNow
          case false => show.DownloadURL.isDefined
        }

    }

  }

}
