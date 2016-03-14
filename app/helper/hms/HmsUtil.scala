package helper.hms

import models.Station

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-03-14
  */
object HmsUtil {

  def getShowsPath(stationId: String, channelId: String): Option[String] = {

    Station.findStation(stationId, channelId).map {

      case None => None

      case Some(station) =>

        val encStationID = java.net.URLEncoder.encode(stationId, "UTF-8")
        Some(
          station
          .getShowUrlPattern
          .replace("{CHANNEL-ID}", channelId)
          .replace("{STATION-ID}", encStationID)
        )

    }

  }

}
