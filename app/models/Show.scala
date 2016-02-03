package models

import constants.VimeoEncodingStatusSystem._
import helper.ShowMetaData
import org.slf4j.LoggerFactory
import play.Logger
import play.api.Play
import play.api.Play.current
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

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

  val log = LoggerFactory.getLogger(this.getClass)

  val showsCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("shows")

  implicit object ShowReads extends Format[Show] {

    override def reads(json: JsValue): JsResult[Show] = {

      val vimeoEncodingStatus = (json \ "vimeoEncodingStatus").asOpt[String]
      val vimeoJson = Json.obj("name" -> vimeoEncodingStatus, "$variant" -> vimeoEncodingStatus)

      val show = Show(
        (json \ "stationId").as[String],
        (json \ "stationName").as[String],
        (json \ "stationLogoUrl").as[String],
        (json \ "stationLogoDisplay").as[Boolean],
        (json \ "stationMainColor").as[String],
        (json \ "channelId").as[String],
        (json \ "channelName").as[String],
        (json \ "showId").as[Long],
        (json \ "showTitle").as[String],
        (json \ "showSourceTitle").as[String],
        (json \ "showSubtitle").as[String],
        (json \ "showLogoUrl").as[String],
        (json \ "showVideoHDUrl").asOpt[String],
        (json \ "showVideoSDUrl").as[String],
        (json \ "channelBroadcastInfo").as[String],
        (json \ "rootPortalURL").as[String],
        (json \ "vimeoId").asOpt[Long],
        vimeoJson.asOpt[VimeoEncodingStatus])

      JsSuccess(show)
    }

    def writes(s: Show) = {
      val vimeoEncodingStatus = if(s.vimeoEncodingStatus.isDefined) s.vimeoEncodingStatus.get.name else ""

      JsObject(Seq(
        "stationId" -> JsString(s.stationId),
        "stationName" -> JsString(s.stationName),
        "stationLogoUrl" -> JsString(s.stationLogoUrl),
        "stationLogoDisplay" -> JsBoolean(s.stationLogoDisplay),
        "stationMainColor" -> JsString(s.stationMainColor),
        "channelId" -> JsString(s.channelId),
        "channelName" -> JsString(s.channelName),
        "showId" -> JsNumber(s.showId),
        "showTitle" -> JsString(s.showTitle),
        "showSourceTitle" -> JsString(s.showSourceTitle),
        "showSubtitle" -> JsString(s.showSubtitle),
        "showLogoUrl" -> JsString(s.showLogoUrl),
        "showVideoHDUrl" -> JsString(s.showVideoHDUrl.getOrElse("")),
        "showVideoSDUrl" -> JsString(s.showVideoSDUrl),
        "channelBroadcastInfo" -> JsString(s.channelBroadcastInfo),
        "rootPortalURL" -> JsString(s.rootPortalURL),
        "vimeoId" -> JsNumber(s.vimeoId.get),
        "vimeoEncodingStatus" -> JsString(vimeoEncodingStatus)
      ))
    }
  }

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

  def findShowVimeoEncodingInProgress: Enumerator[JsObject] = {

    val query = Json.obj("vimeoEncodingStatus" -> IN_PROGRESS)
    val limit = Play.configuration.getInt("vimeo.encoding.batch.size").getOrElse(10)

    log.info(s"query shows with vimeoEncodingStatus=IN_PROGRESS: limit=$limit")

    showsCollection.
      find(query).
      cursor[JsObject].
      enumerate(limit)

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
        )
        Logger.debug("new show doc: " + Json.prettyPrint(Json.toJson(show)))
        showsCollection.insert(Json.toJson(show))
      case None =>
        Logger.debug("found NOT fitting station")
        None
    }
  }
}