package models

import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.MongoDriver

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by dermicha on 06/09/14.
 */
case class Station(stationId: String,
                   hmsStationId: String,
                   channelId: String,
                   active: Boolean,
                   defaultStationName: String,
                   defaultStationLogoUrl: String,
                   defaultStationLogoDisplay: Boolean,
                   defaultStationMainColor: String,
                   defaultChannelName: String,
                   defaultShowTitle: String,
                   defaultShowSubtitle: String,
                   defaultShowLogoUrl: String,
                   defaultChannelBroadcastInfo: String,
                   defaultRootPortalURL: String
                   )

object Station {

  val driver = new MongoDriver
  val connection = driver.connection(List("localhost"))
  val database = connection.db("hbbTVPlugin")
  val stationCollection = database.collection[JSONCollection]("stations")

  implicit val format = Json.format[Station]

  def findStation(stationId: String, channelId: String) = {
    stationCollection.
      // find all people with name `name`
      find(
        Json.obj(
          "stationId" -> stationId,
          "channelId" -> channelId
        ),
        Json.obj()
      ).
      cursor[Station].collect[List](1).map {
      station =>
        station.headOption.map { currentStation => currentStation
        }
    }
  }

  def allStations = stationCollection.find(Json.obj("active" -> true), Json.obj()).
    cursor[Station].collect[List]()
}


