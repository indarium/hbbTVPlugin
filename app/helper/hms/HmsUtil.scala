package helper.hms

import java.util.concurrent.TimeUnit

import helper.Config
import models.Station

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

}
