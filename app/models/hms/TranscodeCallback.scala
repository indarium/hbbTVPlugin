package models.hms

import models.MongoId
import models.dto.ShowMetaData
import play.Logger
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError

import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-02-19
  */
case class TranscodeCallback(_id: Option[MongoId],
                             ID: Long,
                             VerboseMessage: Option[String],
                             Status: String,
                             StatusValue: Option[Int],
                             StatusUnit: Option[String],
                             DownloadSource: Option[String],
                             meta: Option[ShowMetaData]
                            )

object TranscodeCallback {

  val transcodeCallCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("hmsTranscode")

  implicit val reads = Json.reads[TranscodeCallback]
  implicit val writes = Json.writes[TranscodeCallback]

  def insert(jobResult: JobResult, meta: ShowMetaData): Future[LastError] = {

    val transcodeCallback = TranscodeCallback(None, jobResult.ID, jobResult.VerboseResult, "queued", None, None, None, Some(meta))
    transcodeCallCollection.insert(transcodeCallback)

  }

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

  /**
    * Update an existing record or do nothing otherwise.
    *
    * @param callback record with new values (as received from HMS)
    * @return
    */
  def updateRecord(callback: TranscodeCallback) = {

    TranscodeCallback.findByHmsId(callback.ID).map {

      case Some(dbRecord) =>
        val updatedRecord = callback.copy(_id = dbRecord._id, meta = dbRecord.meta)
        TranscodeCallback.save(updatedRecord)

      case None => Logger.error(s"found no transcode record to update: update=$callback")

    }

  }

  def save(transcodeCallback: TranscodeCallback): Future[LastError] = transcodeCallCollection.save(transcodeCallback)

}
