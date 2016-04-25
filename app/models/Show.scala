package models

import constants.VimeoEncodingStatusSystem._
import helper.Config
import models.dto.ShowMetaData
import play.Logger
import play.api.Play.current
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by dermicha on 06/09/14.
  */

case class Show(_id: Option[MongoId],
                stationId: String,
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
                vimeoEncodingStatus: Option[VimeoEncodingStatus],
                s3Name: Option[String]
               )

object Show {

  val showsCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("shows")

  implicit object ShowReads extends Format[Show] {

    override def reads(json: JsValue): JsResult[Show] = {

      val vimeoEncodingStatus = (json \ "vimeoEncodingStatus").asOpt[String]
      val vimeoJson = Json.obj("name" -> vimeoEncodingStatus, "$variant" -> vimeoEncodingStatus)

      val show = Show(
        (json \ "_id").asOpt[MongoId],
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
        vimeoJson.asOpt[VimeoEncodingStatus],
        (json \ "s3Name").asOpt[String]
      )

      JsSuccess(show)
    }

    def writes(s: Show) = {

      val _id = if (s._id.isDefined) MongoId.idFormat.writes(s._id.get) else JsNull

      var seq = Seq(
        "_id" -> _id,
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
        "showLogoUrl" -> JsString(s.showLogoUrl)
      )

      if (s.showVideoHDUrl.isDefined) {
        seq ++= Seq("showVideoHDUrl" -> JsString(s.showVideoHDUrl.get))
      }

      seq ++= Seq(
        "showVideoSDUrl" -> JsString(s.showVideoSDUrl),
        "channelBroadcastInfo" -> JsString(s.channelBroadcastInfo),
        "rootPortalURL" -> JsString(s.rootPortalURL)
      )

      if (s.vimeoId.isDefined) {
        seq ++= Seq("vimeoId" -> JsNumber(s.vimeoId.get))
      }

      if (s.vimeoEncodingStatus.isDefined) {
        seq ++= Seq("vimeoEncodingStatus" -> JsString(s.vimeoEncodingStatus.get.name))
      }

      if (s.s3Name.isDefined) {
        seq ++= Seq("s3Name" -> JsString(s.s3Name.get))
      }

      JsObject(seq)

    }
  }

  def findCurrentShow(stationId: String, channelId: String) = {
    Logger.info("find current show for: %s / %s".format(stationId, channelId))
    showsCollection.
      find(
        Json.obj(
          "stationId" -> stationId,
          "channelId" -> channelId,
          "$or" -> Json.arr(
            Json.obj("vimeoEncodingStatus" -> Json.obj("$exists" -> false)),
            Json.obj("vimeoEncodingStatus" -> DONE.name)
          )
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

  def findShowById(showId: Long) = {
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

  def findShowVimeoEncodingInProgress: Future[Set[Show]] = {

    val query = Json.obj("vimeoEncodingStatus" -> IN_PROGRESS.name)
    val filter = Json.obj()
    val limit = Config.vimeoEncodingBatchSize

    Logger.info(s"query shows with vimeoEncodingStatus=IN_PROGRESS: limit=$limit")

    showsCollection
      .find(query, filter)
      .cursor[JsObject]
      .collect[Set](limit)
      .map { shows =>
        shows.map { currentShow =>
          currentShow.as[Show]
        }
      }
  }

  /**
    * @param stationId stationId to select shows by
    * @return empty if nothing found; not empty otherwise
    */
  def findByStation(stationId: String): Future[Seq[Show]] = {

    val query = Json.obj("stationId" -> stationId)

    showsCollection
      .find(query)
      .cursor[JsObject]
      .collect[Seq]()
      .map {
        shows =>
          shows.map { currentShow =>
            currentShow.as[Show]
          }
      }

  }

  /**
    * Selects all shows for deletion. We keep the n latest ones (with n being the number defined by parameter "skip").
    *
    * @param stationId stationId to select shows by
    * @param skip      skip first n shows
    * @return empty if nothing found; not empty otherwise
    */
  def findForDelete(stationId: String, skip: Int): Future[Seq[Show]] = {

    Logger.info(s"deleteVideo - looking for shows to clean up: stationId=$stationId (keep latest $skip)")

    for (shows <- findByStation(stationId)) yield {

      val (_, remaining) = shows
        .sortWith(sortByShowIdDesc)
        .splitAt(skip)

      Logger.info(s"deleteVideo - found ${remaining.size} $stationId shows to delete")
      remaining

    }

  }

  def createShowByMeta(meta: ShowMetaData) = {
    Logger.info("store show: %s / %s".format(meta.showId, meta.showTitle))

    Station.findStation(meta.stationId, meta.channelId).map {
      case Some(station) =>
        Logger.debug("found fitting station")
        val show = new Show(
          Some(MongoId(MongoId.generate)),
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
          meta.vimeoEncodingStatus,
          meta.s3Name
        )
        Logger.debug("new show doc: " + Json.prettyPrint(Json.toJson(show)))
        showsCollection.insert(Json.toJson(show))
      case None =>
        Logger.debug("found NOT fitting station")
        None
    }
  }

  def update(show: Show): Future[LastError] = showsCollection.save(show) // TODO refactor to return new show object; None if error

  def delete(showId: Long): Unit = {

    val query = Json.obj("showId" -> showId)

    showsCollection
      .remove(query)
      .onComplete {

        case Failure(e) => Logger.error(s"shows - failed to delete record: showId=$showId, e=$e")
        case Success(lastError) => Logger.debug(s"shows - deleted record: showId=$showId")

      }

  }

  private def sortByShowIdDesc(show1: Show, show2: Show) = {
    show1.showId > show2.showId
  }

}