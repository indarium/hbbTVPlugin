package actors

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.event.Logging
import constants.HmsCallbackStatus
import helper._
import helper.hms.{HMSApi, HmsUtil}
import helper.vimeo.VimeoUtil
import models.dto.{ProcessHmsCallback, ShowMetaData}
import models.hms.{HmsShow, TranscodeCallback}
import models.{Show, Station}
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

          log.info(s"creating transcoder job for: ${hmsShow.ID} / ${hmsShow.Name}")
          createTranscodeJob(meta) map {

            case false => self ! ScheduleProcess(processStationData)

            case true =>
              log.info(s"created transcoder job for: ${hmsShow.ID} / ${hmsShow.Name}")
              self ! ScheduleProcess(processStationData)

          }

        case false =>
          meta.sourceVideoUrl = Some(new URL(hmsShow.DownloadURL.get))
          showProcessingActor ! meta

      }

    case ProcessHmsCallback(meta) =>
      log.info("process after HMS callback %s/%s: %s".format(meta.channelId, meta.stationId, meta.sourceVideoUrl))
      showProcessingActor ! meta

    case processStation: ProcessStation =>
      val processingStation: ProcessStationData = processStation.processStationData
      log.info("try to start station processing for: %s (%s)".format(processingStation.stationId, processingStation.hmsStationId))

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
      updateOpenHmsTranscodeJobs
      scheduleHmsStatusUpdate

    case startProcess: StartProcess =>
      log.info("starting show crawler")
      processAllStations
      startVimeoEncodingStatusScheduler
      scheduleHmsStatusUpdate

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

  private def updateOpenHmsTranscodeJobs = {

    log.info("update status of open HMS transcode jobs")

    TranscodeCallback.findByStatusNotFaultyNotFinished map {

      openTranscodeJobs =>

        openTranscodeJobs.isEmpty match {

          case true => log.info("found no transcode jobs to update")

          case false =>
            for (transcodeCallback <- openTranscodeJobs) {

              log.info(s"attempt status update of open transcode job: station=${transcodeCallback.meta.get.stationId}, ID=${transcodeCallback.ID}")
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

}
