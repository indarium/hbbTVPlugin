package models

import constants.{DownloadQueueStatus, JsonConstants}
import helper.Config
import models.dto.ShowMetaData
import org.joda.time.DateTime
import play.api.Logger
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
  * since: 2016-04-13
  */
case class DownloadQueue(meta: ShowMetaData,
                         status: String = DownloadQueueStatus.open,
                         retryCount: Int = 0,
                         created: DateTime = DateTime.now,
                         modified: DateTime = DateTime.now,
                         nextRun: DateTime = DateTime.now.plusSeconds(Config.downloadRetryFirst)
                        )

object DownloadQueue {

  private val downloadQueueCollection = ReactiveMongoPlugin.db.collection[BSONCollection]("downloadQueue")

  implicit val dateReads = Reads.jodaDateReads(JsonConstants.dateFormat)
  implicit val dateWrites = Writes.jodaDateWrites(JsonConstants.dateFormat)

  // bson DateTime based on: https://gist.github.com/ctcarrier/9918087
  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value)

    def write(time: DateTime) = BSONDateTime(time.getMillis)
  }

  implicit val reads = Json.reads[DownloadQueue]
  implicit val writes = Json.writes[DownloadQueue]

  implicit val bsonHandler = Macros.handler[DownloadQueue]

  def insert(downloadQueue: DownloadQueue): Future[LastError] = downloadQueueCollection.insert(downloadQueue)

  /**
    * Create or update downloadQueue record. This includes automatically setting the status and nextRun fields to
    * appropriate values.
    *
    * @param meta basis for the new record
    * @return
    */
  def queueDownload(meta: ShowMetaData): Future[LastError] = {

    findByShowIdNotFailed(meta.showId.get) flatMap {

      case None =>
        val retryDownload = DownloadQueue(meta)
        insert(retryDownload)

      case Some(record) =>
        val modified = record.copy(
          status = newRetryStatus(record),
          nextRun = nextRetryDate(record),
          meta = meta
        )
        update(modified)

    }

  }

  def update(downloadQueue: DownloadQueue): Future[LastError] = {

    val modified = downloadQueue.copy(modified = DateTime.now)
    val selector = BSONDocument("meta.showId" -> downloadQueue.meta.showId.get)
    downloadQueueCollection.update(selector, modified)

  }

  def delete(downloadQueue: DownloadQueue): Unit = {

    downloadQueue.meta.showId match {

      case None => Logger.error("downloadQueue - unable to delete record with missing meta.showId")

      case Some(showId) =>

        val query = BSONDocument("meta.showId" -> showId)
        downloadQueueCollection.remove(query)
          .onComplete {

            case Failure(e) => Logger.error(s"downloadQueue - failed to delete record: showId=$showId, e=$e")
            case Success(lastError) => Logger.debug(s"downloadQueue - deleted record: showId=$showId")

          }

    }

  }

  /**
    * Gives us all downloads scheduled next (oldest records first).
    *
    * @return empty list if nothing is found
    */
  def findScheduledNext: Future[Seq[DownloadQueue]] = {

    countInProgress flatMap {

      inProgressCount =>

        val limit = Config.downloadParallelMax - inProgressCount
        val selector = BSONDocument(
          "status" -> DownloadQueueStatus.open,
          "nextRun" -> BSONDocument("$lte" -> new DateTime)
        )
        val sort = BSONDocument("nextRun" -> 1)

        downloadQueueCollection
          .find(selector)
          .sort(sort)
          .cursor[BSONDocument]
          .collect[Seq](limit)
          .map {
            seq => seq map (bson => BSON.readDocument[DownloadQueue](bson))
          }

    }


  }

  /**
    * Reset all downloadQueue records from status "in_progress" to "open".
    */
  def resetInProgressToOpen(): Unit = {

    for (inProgressSeq <- findStatusInProgress) yield {

      inProgressSeq foreach { inProgress =>

        val open = inProgress.copy(status = DownloadQueueStatus.open)
        DownloadQueue.update(open)

      }

    }

  }

  /**
    * @param status status on which count is based
    * @return number of records with given status
    */
  def countWithStatus(status: String): Future[Int] = {

    val selector = BSONDocument("status" -> status)

    downloadQueueCollection
      .find(selector)
      .cursor[BSONDocument]
      .collect[Seq]()
      .map {
        seq => seq.size
      }

  }

  /**
    * @return all records with status "in_progress"
    */
  def findStatusInProgress: Future[Seq[DownloadQueue]] = findByStatus(DownloadQueueStatus.in_progress)

  /**
    * @param status status on which selection is based
    * @return all records with given status
    */
  def findByStatus(status: String): Future[Seq[DownloadQueue]] = {

    val selector = BSONDocument("status" -> status)

    downloadQueueCollection
      .find(selector)
      .cursor[BSONDocument]
      .collect[Seq]()
      .map {
        seq => seq map (bson => BSON.readDocument[DownloadQueue](bson))
      }

  }

  /**
    * @return number of records with status "in_progress"
    */
  def countInProgress: Future[Int] = countWithStatus(DownloadQueueStatus.in_progress)

  /**
    * @return None if nothing's found
    */
  def findByShowId(showId: Long): Future[Seq[DownloadQueue]] = {

    val selector = BSONDocument("meta.showId" -> Some(showId))

    downloadQueueCollection
      .find(selector)
      .cursor[BSONDocument]
      .collect[Seq]()
      .map {
        set => {
          set map {
            bson => BSON.readDocument[DownloadQueue](bson)
          }
        }
      }

  }

  /**
    * @return None if nothing's found
    */
  def findByShowIdNotFailed(showId: Long): Future[Option[DownloadQueue]] = {

    val selector = BSONDocument(
      "meta.showId" -> Some(showId),
      "status" -> BSONDocument("$ne" -> DownloadQueueStatus.failed)
    )

    downloadQueueCollection
      .find(selector)
      .cursor[BSONDocument]
      .collect[Set]()
      .map {
        set => {
          set.headOption map {
            bson => BSON.readDocument[DownloadQueue](bson)
          }
        }
      }

  }

  /**
    * @param meta we search the Show record based on meta
    * @return
    */
  def deleteIfExists(meta: ShowMetaData) = {

    meta.showId match {

      case None => Logger.debug(s"downloadQueue - unable to do housekeeping for missing meta.showId: meta=$meta")

      case Some(showId) =>
        for {
          Some(downloadQueue) <- DownloadQueue.findByShowIdNotFailed(showId)
        } yield {
          DownloadQueue.delete(downloadQueue)
        }

    }

  }

  /**
    * Gives us the new status for a DownloadQueue record before writing it back to the database.
    *
    * @param record if record.retryCount has reached the maximum number retries we'll return "failed"
    * @return "failed" if retryCount has reached max number of retries; otherwise the input param preferredStatus
    */
  private def newRetryStatus(record: DownloadQueue): String = {

    // TODO unit tests
    // TODO extract to util class
    val maxRetry = Config.downloadRetryMax

    record.retryCount >= maxRetry match {

      case true => DownloadQueueStatus.failed
      case false => DownloadQueueStatus.open

    }

  }

  private def nextRetryDate(record: DownloadQueue): DateTime = {

    // TODO unit tests
    // TODO extract to util class
    val delay = Config.downloadRetryAfterFirst * record.retryCount
    DateTime.now.plusSeconds(delay)

  }

}