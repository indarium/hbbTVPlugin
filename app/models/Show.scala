package models

import constants.VimeoEncodingStatusSystem._
import helper.ShowMetaData
import play.Logger
import play.api.Play
import play.api.Play.current
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
                showSourceTitle: String,
                showSubtitle: String,
                showLogoUrl: String,
                showVideoHDUrl: Option[String],
                showVideoSDUrl: String,
                channelBroadcastInfo: String,
                rootPortalURL: String,
                vimeoId: Option[Long],
                vimeoEncodingStatus: Option[VimeoEncodingStatus]
                 )

object Show {

  val showsCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("shows")

  implicit val format = Json.format[Show]

  def findCurrentShow(stationId: String, channelId: String) = {
    Logger.info("find current show for: %s / %s".format(stationId, channelId))
    showsCollection.
      find(
        Json.obj(
          "stationId" -> stationId,
          "channelId" -> channelId
        ),
        Json.obj("_id" -> 0
          , "showId" -> 0
        )
      )
      .sort(Json.obj("showId" -> -1))
      .cursor[JsObject].collect[List](1).map {
      show =>
        show.headOption.map { currentShowMeta => currentShowMeta.as[JsObject]
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

  def findShowVimeoEncodingInProgress: Future[List[JsObject]] = {

    val query = Json.obj("vimeoEncodingStatus" -> IN_PROGRESS.vimeoEncodingStatus)
    val limit = Play.configuration.getInt("vimeo.encoding.batch.size").getOrElse(10)

    showsCollection.
      find(query).
      cursor[JsObject].
      collect[List](limit)

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
          meta.showSourceTitle.getOrElse("no source title"),
          meta.showSubtitle.getOrElse(station.defaultShowSubtitle),
          station.defaultShowLogoUrl,
          None,
          meta.publicVideoUrl.get.toString,
          station.defaultChannelBroadcastInfo,
          station.defaultRootPortalURL,
          meta.vimeoId,
          meta.vimeoEncodingStatus
//          if(meta.vimeoEncodingStatus.isDefined) Some(meta.vimeoEncodingStatus.get.vimeoEncodingStatus) else None
        )
        Logger.debug("new show doc: " + Json.prettyPrint(Json.toJson(show)))
        showsCollection.insert(Json.toJson(show))
      case None =>
        Logger.debug("found NOT fitting station")
        None
    }
  }
}