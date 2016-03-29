package models.hms

import constants.{HmsCallbackStatus, JsonConstants}
import models.dto.ShowMetaData
import org.joda.time.DateTime
import play.Logger
import play.api.Play.current
import play.api.libs.json.{Json, Reads, Writes}
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-02-19
  */
case class TranscodeCallback(ID: Long,
                             VerboseMessage: Option[String],
                             Status: String,
                             StatusValue: Option[Int],
                             StatusUnit: Option[String],
                             DownloadSource: Option[String],
                             meta: Option[ShowMetaData],
                             created: Option[DateTime] = Some(DateTime.now),
                             modified: Option[DateTime] = Some(DateTime.now)
                            )

object TranscodeCallback {

  private val transcodeCallCollection = ReactiveMongoPlugin.db.collection[BSONCollection]("hmsTranscode")

  implicit val dateReads = Reads.jodaDateReads(JsonConstants.dateFormat)
  implicit val dateWrites = Writes.jodaDateWrites(JsonConstants.dateFormat)

  // bson DateTime based on: https://gist.github.com/ctcarrier/9918087
  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
  }

  implicit val reads = Json.reads[TranscodeCallback]
  implicit val writes = Json.writes[TranscodeCallback]

  implicit val bsonHandler = Macros.handler[TranscodeCallback]

  def insert(jobResult: JobResult, meta: ShowMetaData): Future[LastError] = {

    val transcodeCallback = TranscodeCallback(jobResult.ID, jobResult.VerboseResult, HmsCallbackStatus.QUEUED, None, None, None, Some(meta))
    transcodeCallCollection.insert(transcodeCallback)

  }

  def findByHmsId(hmsId: Long): Future[Option[TranscodeCallback]] = {

    val selector = BSONDocument("ID" -> hmsId)

    transcodeCallCollection
      .find(selector)
      .cursor[BSONDocument]
      .collect[Set](1)
      .map {
        set => {
          // TODO do we return None if set is empty?
          set.headOption.map {
            bson => BSON.readDocument[TranscodeCallback](bson)
          }
        }
      }

  }

  def findByShowIdWithStatusNotFaulty(showId: Long): Future[Option[TranscodeCallback]] = {

    val selector = BSONDocument(
      "meta.showId" -> showId,
      "Status" -> BSONDocument("$ne" -> HmsCallbackStatus.FAULTY)
    )

    transcodeCallCollection
      .find(selector)
      .cursor[BSONDocument]
      .collect[Set](1)
      .map {
        set => {
          // TODO do we return None if set is empty?
          set.headOption.map {
            bson => BSON.readDocument[TranscodeCallback](bson)
          }
        }
      }

  }

  /**
    * @return empty set if we don't find any matching records
    */
  def findByStatusNotFaultyNotFinished: Future[Set[TranscodeCallback]] = {

    val selector = BSONDocument(
      "Status" ->
        BSONDocument(
          "$not" -> BSONDocument(
            "$in" -> BSONArray(HmsCallbackStatus.FINISHED, HmsCallbackStatus.FAULTY)
          )
        )
    )

    transcodeCallCollection
      .find(selector)
      .cursor[BSONDocument]
      .collect[Set]()
      .map {
        set => {
          set.map {
            bson => BSON.readDocument[TranscodeCallback](bson)
          }
        }
      }

  }

  /**
    * Update an existing record or do nothing otherwise.
    *
    * @param callback record with new values (as received from HMS...meaning that it contains no internal fields like
    *                 <i>meta</i>, <i>created</i> or <i>modified</i>)
    * @return
    */
  def updateRecord(callback: TranscodeCallback): Unit = {

    try {

      findByHmsId(callback.ID).map {

        case Some(dbRecord) =>

          val updatedRecord = callback.copy(meta = dbRecord.meta, created = dbRecord.created, modified = dbRecord.modified)

          update(updatedRecord).onComplete {
            case Failure(e) => Logger.error(s"updateRecord() - failed to update hmsTranscode record: callback=$callback, e=", e)
            case Success(lastError) => Logger.debug(s"updated hmsTranscode record: $updatedRecord")
          }

        case None => Logger.error(s"found no transcode record to update: update=$callback")

      }

    } catch {
      case e: Exception => Logger.error(s"failed to update callback record: $callback, exception=$e")
    }

  }

  def save(transcodeCallback: TranscodeCallback): Future[LastError] = transcodeCallCollection.save(transcodeCallback)

  def update(transcodeCallback: TranscodeCallback): Future[LastError] = {

    val newModified = transcodeCallback.copy(modified = Some(DateTime.now))
    val selector = BSONDocument("ID" -> newModified.ID)
    transcodeCallCollection.update(selector, newModified)

  }

}
