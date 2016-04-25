package models

import play.Logger
import play.api.Play.current
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-04-25
  */
case class DeleteShow(showId: Long,
                      stationId: String,
                      showVideoSDUrl: String,
                      vimeoId: Option[Long]
                     )

object DeleteShow {

  implicit val format = Json.format[DeleteShow]

  val showsCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("shows")

  def findShowById(showId: Long): Future[Option[DeleteShow]] = {
    showsCollection.
      find(
        Json.obj("showId" -> showId),
        Json.obj()
      ).
      cursor[JsObject].collect[List](1).map {
      show =>
        show.headOption.map { currentShow => currentShow.as[DeleteShow]
        }
    }
  }

  /**
    * @param stationId stationId to select shows by
    * @return empty if nothing found; not empty otherwise
    */
  def findByStation(stationId: String): Future[Seq[DeleteShow]] = {

    val query = Json.obj("stationId" -> stationId)

    showsCollection
      .find(query)
      .cursor[JsObject]
      .collect[Seq]()
      .map {
        shows =>
          shows.map { currentShow =>
            currentShow.as[DeleteShow]
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
  def findForDelete(stationId: String, skip: Int): Future[Seq[DeleteShow]] = {

    Logger.info(s"deleteVideo - looking for shows to clean up: stationId=$stationId (keep latest $skip)")

    for (shows <- findByStation(stationId)) yield {

      val (_, remaining) = shows
        .sortWith(sortByShowIdDesc)
        .splitAt(skip)

      Logger.info(s"deleteVideo - found ${remaining.size} $stationId shows to delete")
      remaining

    }

  }

  private def sortByShowIdDesc(show1: DeleteShow, show2: DeleteShow) = {
    show1.showId > show2.showId
  }

}
