package actors

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.event.Logging
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
            case true => log.info(s"created transcoder job for: ${hmsShow.ID} / ${hmsShow.Name}")
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
          log.error(e, "could not start process")
          self ! ScheduleProcess(processingStation)
      }
      f.map {

        case None =>
          log.error(s"could not start process: stationId=${processingStation.stationId}")
          self ! ScheduleProcess(processingStation)

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

    case startProcess: StartProcess =>
      log.info("starting show crawler")
      processAllStations
      startVimeoEncodingStatusScheduler

    case scheduleProcess: ScheduleProcess =>
      log.info("scheduling show crawler")
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

      case _ =>
        log.error(s"createTranscodeJob() - unable to persist missing JobResult: meta=$meta")
        Future(false)

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

}
