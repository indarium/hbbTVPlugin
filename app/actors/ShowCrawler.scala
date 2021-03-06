package actors

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.event.Logging
import constants.{DownloadQueueStatus, HmsCallbackStatus}
import helper._
import helper.hms.{HMSApi, HmsUtil}
import helper.vimeo.VimeoUtil
import models.dto.{RetryDownload, ShowMetaData}
import models.hms.{HmsShow, TranscodeCallback}
import models.{DownloadQueue, Show, Station}
import reactivemongo.core.commands.LastError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by dermicha on 07/09/14.
  */


case class ProcessStationData(hmsStationId: String, stationId: String, channelId: String)

case class ProcessShowData(show: HmsShow, processStationData: ProcessStationData)

case class StartProcess()

case class ProcessShow(processShowData: ProcessShowData)

case class ProcessStation(processStationData: ProcessStationData)

case class ScheduleProcess(processStationData: ProcessStationData)

case class ScheduleHmsStatusUpdate()

case class ScheduleDownloadQueue()

case class ScheduleCleanUp()

class ShowCrawler extends Actor {
  val log = Logging(context.system, this)

  val crawlerPeriod = Config.hmsCrawlerPeriod

  val vimeoAccessToken: String = Config.vimeoAccessToken
  val vimeoBackend: VimeoBackend = new VimeoBackend(vimeoAccessToken)

  private val s3Backend: S3Backend = S3Util.backend
  val showProcessingActor = context.actorOf(Props(new ShowProcessingActor(s3Backend)))

  def receive = {
    case processShow: ProcessShow =>

      val hmsShow: HmsShow = processShow.processShowData.show
      val processStationData: ProcessStationData = processShow.processShowData.processStationData
      log.info(s"starting show processing for: ${hmsShow.ID} / ${hmsShow.Name}")

      val meta = new ShowMetaData(processStationData.stationId, processStationData.channelId)
      meta.hmsStationId = Some(processStationData.hmsStationId)
      meta.showId = Some(hmsShow.ID)
      meta.showTitle = hmsShow.Name
      meta.showSourceTitle = meta.showTitle

      log.info("check for vimeo exception stuff!!")
      if (VimeoUtil.uploadActivated(meta.stationId)) {
        log.info(s"found mvv tv station ${meta.hmsStationId}")
        meta.vimeo = Some(true)
      }

      log.info("collected meta: " + meta.showTitle + " / " + meta.sourceVideoUrl)

      HmsUtil.isTranscoderEnabled(meta.stationId) match {

        case true =>

          log.info(s"creating transcoder job for: ${hmsShow.ID} / ${hmsShow.Name} / ${meta.stationId}")
          createTranscodeJob(meta) map {

            case false => self ! ScheduleProcess(processStationData)

            case true =>
              log.info(s"created transcoder job for: ${hmsShow.ID} / ${hmsShow.Name} / ${meta.stationId}")
              self ! ScheduleProcess(processStationData)

          }

        case false =>
          meta.sourceVideoUrl = Some(new URL(hmsShow.DownloadURL.get))
          showProcessingActor ! meta

      }

    case RetryDownload(download) =>
      val meta = download.meta
      log.info(s"retry download ${meta.channelId}/${meta.stationId}: ${meta.sourceVideoUrl}")
      showProcessingActor ! meta

    case processStation: ProcessStation =>
      val processingStation: ProcessStationData = processStation.processStationData
      val f = HMSApi.getCurrentShow(processStation.processStationData.stationId, processStation.processStationData.channelId)
      f.onFailure {
        case e: Exception =>
          log.error(e, s"could not start process: stationId=${processingStation.stationId}")
          self ! ScheduleProcess(processingStation)
      }
      f.map {

        case None => self ! ScheduleProcess(processingStation)

        case Some(show) =>

          for {
            existingShow <- Show.findShowById(show.ID)
            transcodeCallback <- TranscodeCallback.findByShowIdWithStatusNotFaulty(show.ID)
          } yield {

            // TODO this check is done in a lower layer: remove
            existingShow.isDefined || transcodeCallback.isDefined match {

              case true =>
                log.info("nothing to do for: %s / %s ".format(show.ID, show.Name))
                self ! ScheduleProcess(processingStation)

              case false =>
                log.info("starting station processing for: %s (%s)".format(processingStation.stationId, processingStation.hmsStationId))
                self ! ProcessShow(ProcessShowData(show, processingStation))

            }

          }

      }

    case scheduleTranscodeJobStatusUpdate: ScheduleHmsStatusUpdate =>
      resetOpenHmsTranscodeJobs
      scheduleHmsStatusUpdate

    case processDownloads: ScheduleDownloadQueue =>
      processDownloadQueue
      scheduleDownloadQueue()

    case cleanUp: ScheduleCleanUp =>
      processCleanUp
      scheduleCleanUpJob()

    case startProcess: StartProcess =>
      log.info("starting show crawler")
      processAllStations
      startVimeoEncodingStatusScheduler
      scheduleHmsStatusUpdate
      resetDownloadQueue()
      scheduleDownloadQueue(Config.downloadQueueStartDelay)
      scheduleCleanUpJob(Config.cleanUpJobStartUpDelay)

    case scheduleProcess: ScheduleProcess =>
      log.info(s"scheduling show crawler (${scheduleProcess.processStationData.stationId})")
      context.system.scheduler.scheduleOnce(
        Duration.create(crawlerPeriod, TimeUnit.MINUTES),
        self,
        ProcessStation(scheduleProcess.processStationData))

  }

  /**
    * @param meta contains all information needed to call HMS's transcode method
    * @return true if everything worked, false if we had an error
    */
  private def createTranscodeJob(meta: ShowMetaData): Future[Boolean] = {

    HMSApi.transcode(meta) flatMap {

      case Some(jobResult) =>

        TranscodeCallback.insert(jobResult, meta).map {

          case le: LastError if le.inError =>
            log.error(s"createTranscodeJob() - failed to insert jobResult: lastError=$le")
            false

          case _ => true

        }

      case _ => Future(false)

    }

  }

  private def resetOpenHmsTranscodeJobs = {

    log.info("update status of open HMS transcode jobs")

    TranscodeCallback.findByStatusNotFaultyNotFinished map {

      openTranscodeJobs =>

        openTranscodeJobs.isEmpty match {

          case true => log.info("found no transcode jobs to update")

          case false =>
            for (transcodeCallback <- openTranscodeJobs) {

              log.info(s"attempt status update of open transcode job: station=${transcodeCallback.meta.get.stationId}, ID=${transcodeCallback.ID}, currentStatus=${transcodeCallback.Status}")
              queryHmsStatus(transcodeCallback) map {
                jobStatus => updateJobStatusUnlessFinished(jobStatus)
              }

            }

        }

    }

  }

  private def queryHmsStatus(transcodeCallback: TranscodeCallback): Future[Option[TranscodeCallback]] = {

    transcodeCallback.meta match {

      case None =>
        log.error(s"unable to query transcode job status query for missing meta: ID=${transcodeCallback.ID}")
        Future(None)

      case Some(meta) =>
        val channelId = meta.channelId
        val jobId = transcodeCallback.ID
        HMSApi.transcodeJobStatus(channelId, jobId)

    }

  }

  private def updateJobStatusUnlessFinished(jobStatus: Option[TranscodeCallback]) = {

    jobStatus match {

      case Some(update) if update.Status != HmsCallbackStatus.FINISHED =>
        TranscodeCallback.updateRecord(update)

      case Some(update) if update.Status == HmsCallbackStatus.FINISHED =>
        log.info(s"transcode job has finished, waiting for callback: ID=${update.ID}")

    }

  }

  private def processDownloadQueue = {

    DownloadQueue.findScheduledNext map { openDownloads =>

      openDownloads foreach { download =>

        val meta = download.meta
        meta.showId match {

          case None =>
            log.error(s"downloadQueue - unable to retry download for show with missing showId: meta=${download.meta}")
            val failed = download.copy(status = DownloadQueueStatus.failed)
            DownloadQueue.update(failed)

          case Some(showId) =>

            Show.findShowById(showId) map {

              case Some(show) =>

                log.info(s"downloadQueue - delete record for existing show: showId=$showId, station=${meta.stationId}")
                DownloadQueue.delete(download)

              case None =>

                val inProgress: DownloadQueue = download.copy(status = DownloadQueueStatus.in_progress, retryCount = download.retryCount + 1)
                DownloadQueue.update(inProgress)

                val meta = inProgress.meta
                log.info(s"downloadQueue - retry: show=${meta.showId}, station=${meta.stationId}, retryCount=${download.retryCount}")
                self ! RetryDownload(inProgress)

            }

        }

      }

    }

  }

  private def processCleanUp = {

    Station.findForDeleteOldShows map {
      _.foreach(stationCleanUp)
    }

  }

  private def stationCleanUp(station: Station): Unit = {

    if (station.keepLastShows.isDefined) {

      val keepLastShows = station.keepLastShows.get

      Show.findForDelete(station.stationId, keepLastShows) map {
        _.foreach(VideoUtil.deleteAllRecords)
      }

    }

  }

  private def processAllStations = {

    Station.allStations.map { stations =>

      log.debug("found stations: " + stations.iterator.length)
      var count: Int = 0

      stations.foreach { station =>
        count += 10
        log.info("will launch processing of station: %s in %d seconds ".format(station.stationId, count))
        context.system.scheduler.scheduleOnce(
          Duration.create(count, TimeUnit.SECONDS),
          self,
          ProcessStation(ProcessStationData(station.hmsStationId, station.stationId, station.channelId)))
      }

    }

  }

  private def startVimeoEncodingStatusScheduler = {

    val delay = Duration.create(20, TimeUnit.SECONDS)
    val intervalConfig = Config.vimeoEncodingCheckInterval
    val interval = Duration.create(intervalConfig, TimeUnit.SECONDS)
    val vimeoVideoStatusActor = context.actorOf(Props(new VimeoVideoStatusActor()))

    log.debug(s"scheduling VimeoVideoStatusActor: delay=$delay, interval=$interval")
    context.system.scheduler.schedule(delay, interval, vimeoVideoStatusActor, QueryVimeoVideoStatus)

  }

  private def scheduleHmsStatusUpdate = {
    val length = Config.hmsTranscodeStatusUpdateInterval
    log.info(s"schedule next update of open HMS Transcode Jobs to happen in $length seconds.")
    val delay = Duration.create(length, TimeUnit.SECONDS)
    context.system.scheduler.scheduleOnce(delay, self, ScheduleHmsStatusUpdate())
  }

  private def resetDownloadQueue(): Unit = {
    log.info(s"downloadQueue - reset records from '${DownloadQueueStatus.in_progress}' to '${DownloadQueueStatus.open}'")
    DownloadQueue.resetInProgressToOpen()
  }

  private def scheduleDownloadQueue(delay: Long = Config.downloadQueueRetryInterval) = {
    log.debug(s"schedule next download queue update to run in $delay seconds")
    val duration = Duration.create(delay, TimeUnit.SECONDS)
    context.system.scheduler.scheduleOnce(duration, self, ScheduleDownloadQueue())
  }

  private def scheduleCleanUpJob(delay: Int = Config.cleanUpJobInterval): Unit = {
    log.debug(s"deleteVideo - schedule next cleanUp job to run in $delay seconds")
    val duration = Duration.create(delay, TimeUnit.SECONDS)
    context.system.scheduler.scheduleOnce(duration, self, ScheduleCleanUp())
  }

}
