package helper.hms

import java.util.concurrent.TimeUnit

import helper.Config
import models.Station

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * author: cvandrei
  * since: 2016-03-14
  */
object HmsUtil {

  def getShowsUrl(stationId: String, channelId: String): Option[String] = {

    val stationFuture = for {
      s <- Station.findStation(stationId, channelId)
    } yield s

    val stationOpt = Await.result(stationFuture, Duration(5, TimeUnit.SECONDS))
    stationOpt match {

      case None => None

      case Some(station) =>

        val baseUrl = Config.hmsBroadcastUrl
        val encStationID = java.net.URLEncoder.encode(stationId, "UTF-8")
        val encHmsStationID = java.net.URLEncoder.encode(station.hmsStationId, "UTF-8")

        val path = station
          .getShowUrlPattern.getOrElse("/Show/{CHANNEL-ID}?Category={STATION-ID}&Order=DESC&Count=25")
          .replace("{CHANNEL-ID}", channelId)
          .replace("{STATION-ID}", encStationID)
          .replace("{HMS-STATION-ID}", encHmsStationID)
        val url = baseUrl + path

        Some(url)

    }

  }

}
