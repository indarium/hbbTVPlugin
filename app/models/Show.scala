package models

import helper.ShowMetaData
import play.Logger
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.MongoDriver

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by dermicha on 06/09/14.
 */

case class Show(stationId: String,
                stationName: String,
                stationLogoUrl: String,
                stationLogoDisplay: Boolean,
                stationMainColor: String,
                channelId: String,
                channelName: String,
                showId: Long,
                showTitle: String,
                showSubtitle: String,
                showLogoUrl: String,
                showVideoHDUrl: Option[String],
                showVideoSDUrl: String,
                channelBroadcastInfo: String,
                rootPortalURL: String
                 )

object Show {

  val driver = new MongoDriver
  val connection = driver.connection(List("localhost"))
  val database = connection.db("hbbTVPlugin")
  val showsCollection = database.collection[JSONCollection]("shows")

  implicit val format = Json.format[Show]

  def findCurrentShow(stationId: String, channelId: String) = {
    Logger.info("find current show for: %s / %s".format(stationId, channelId))
    showsCollection.
      find(
        Json.obj(
          "stationId" -> stationId,
          "channelId" -> channelId
        ),
        Json.obj("_id" -> 0)
      )
      .sort(Json.obj("showId" -> -1))
      .cursor[JsObject].collect[List](1).map {
      show =>
        show.headOption.map { currentShowMeta => currentShowMeta.as[Show]
        }
    }
  }

  def findShowById(showId: Int) = {
    showsCollection.
      // find all people with name `name`
      find(
        Json.obj(
          "showId" -> showId
        ),
        Json.obj()
      ).
      cursor[JsObject].collect[List](1).map {
      show =>
        show.headOption.map { currentShowMeta => currentShowMeta.as[Show]
        }
    }
  }

  def createShowByMeta(meta: ShowMetaData) = {
    Logger.info("store show: %s / %s".format(meta.showId, meta.showTitle))

    Station.findStation(meta.stationId, meta.channelId).map {
      case Some(station) =>
        Logger.debug("found fitting station")
        val show = new Show(
          meta.stationId,
          meta.stationName.getOrElse(station.defaultStationName),
          station.defaultStationLogoUrl,
          station.defaultStationLogoDisplay,
          meta.stationMainColor.getOrElse(station.defaultStationMainColor),
          meta.channelId,
          meta.channelName.getOrElse(station.defaultChannelName),
          meta.showId.get,
          station.defaultShowTitle,
          meta.showSubtitle.getOrElse(station.defaultShowSubtitle),
          station.defaultShowLogoUrl,
          None,
          meta.publicVideoUrl.get.toString,
          station.defaultChannelBroadcastInfo,
          station.defaultRootPortalURL
        )
        Logger.debug("new show doc: " + Json.prettyPrint(Json.toJson(show)))
        showsCollection.insert(Json.toJson(show))
      case None =>
        Logger.debug("found NOT fitting station")
        None
    }
  }
}