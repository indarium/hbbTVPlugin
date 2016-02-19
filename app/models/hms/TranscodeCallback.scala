package models.hms

import models.MongoId
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-02-19
  */
case class TranscodeCallback(_id: Option[MongoId],
                             ID: Long,
                             VerboseMessage: String,
                             Status: String,
                             StatusValue: Option[Int],
                             StatusUnit: Option[String],
                             DownloadSource: Option[String])

object TranscodeCallback {

  val transcodeCallCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("hmsTranscode")

  implicit val reads = Json.reads[TranscodeCallback]
  implicit val writes = Json.writes[TranscodeCallback]

  def findByHmsId(hmsId: Long): Future[Option[TranscodeCallback]] = {

    val selector = Json.obj("ID" -> hmsId)

    transcodeCallCollection
      .find(selector)
      .cursor[JsObject]
      .collect[Set](1)
      .map {
        set => {
          set.headOption.map {
            json =>
              json.as[TranscodeCallback]
          }
        }
      }

  }

  def update(transcodeCallback: TranscodeCallback): Future[LastError] = transcodeCallCollection.save(transcodeCallback)

}
