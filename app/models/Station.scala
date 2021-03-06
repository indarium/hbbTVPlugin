package models

import models.dto.ShowMetaData
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
                   defaultRootPortalURL: String,
                   getShowUrlPattern: Option[String],
                   keepLastShows: Option[Int],
                   hmsEncodingProfile: Option[String]
                   )

object Station {

  val stationCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("stations")

  implicit val format = Json.format[Station]

  def findStation(meta: ShowMetaData): Future[Option[Station]] = findStation(meta.stationId, meta.channelId)

  def findStation(stationId: String, channelId: String): Future[Option[Station]] = {
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

  def findStation(stationId: String): Future[Option[Station]] = {
    stationCollection.
      // find all people with name `name`
      find(
        Json.obj(
          "stationId" -> stationId
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

  def findForDeleteOldShows: Future[Seq[Station]] = {

    val query = Json.obj(
      "keepLastShows" -> Json.obj("$exists" -> true),
      "keepLastShows" -> Json.obj("$gt" -> 0)
    )

    stationCollection
      .find(query)
      .cursor[JsObject]
      .collect[Seq]()
      .map {
        stations =>
          stations.map { station =>
            station.as[Station]
          }
      }

  }

}