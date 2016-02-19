package actors

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.event.Logging
import com.amazonaws.auth.BasicAWSCredentials
import helper._
import models.dto.ShowMetaData
import models.{Show, Station}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by dermicha on 07/09/14.
  */


case class ProcessStationData(hmsStationId: String, stationId: String, channelId: String)

case class ProcessShowData(show: HMSShow, processStationData: ProcessStationData)

case class StartProcess()

case class ProcessShow(processShowData: ProcessShowData)

case class ProcessStation(processStationData: ProcessStationData)

case class ScheduleProcess(processStationData: ProcessStationData)

class ShowCrawler extends Actor {
  val log = Logging(context.system, this)

  val crawlerPeriod = Config.hmsCrawlerPeriod

  val awsAccessKeyId: String = Config.awsAccessKeyId
  val awsSecretKey: String = Config.awsSecretKey
  val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
  val s3Backend: S3Backend = new S3Backend(credentials, Config.awsBucket)

  val vimeoAccessToken: String = Config.vimeoAccessToken
  val vimeoBackend: VimeoBackend = new VimeoBackend(vimeoAccessToken)

  val mmv = List("mv1", "wis") //TODO refactor to make this list configurable

  val showProcessingActor = context.actorOf(Props(new ShowProcessingActor(s3Backend)))

  def receive = {
    case processShow: ProcessShow =>

      val hmsShow: HMSShow = processShow.processShowData.show
      val processStationData: ProcessStationData = processShow.processShowData.processStationData
      log.info("starting show processing for: %d / %s".format(hmsShow.ID, hmsShow.Name))

      val meta = new ShowMetaData(processStationData.stationId, processStationData.channelId)
      meta.hmsStationId = Some(processStationData.hmsStationId)
      meta.showId = Some(hmsShow.ID)
      meta.showTitle = hmsShow.Name
      meta.sourceVideoUrl = Some(new URL(hmsShow.DownloadURL.get))

      log.info("check for vimeo exception stuff!!")
      if (mmv.contains(meta.stationId.toLowerCase)) {
        log.info(s"found mvv tv station ${meta.hmsStationId}")
        meta.vimeo = Some(true)
      }

      log.info("collected meta: " + meta.showTitle + " / " + meta.sourceVideoUrl)
      showProcessingActor ! meta

    case processStation: ProcessStation =>
      log.info("try to start station processing for: %s (%s)".format(processStation.processStationData.stationId, processStation.processStationData.hmsStationId))

      val f = HMSApi.getCurrentShow(processStation.processStationData.hmsStationId, processStation.processStationData.channelId)
      f.onFailure {
        case e: Exception =>
          log.error(e, "could not start process")
          self ! ScheduleProcess(processStation.processStationData)
      }
      f.map {
        case Some(show) =>
          Show.findShowById(show.ID).map {
            case Some(existingShow) =>
              log.info("nothing to do for: %s / %s ".format(show.ID, show.Name))
              self ! ScheduleProcess(processStation.processStationData)
            case None =>
              log.info("starting station processing for: %s (%s)".format(processStation.processStationData.stationId, processStation.processStationData.hmsStationId))
              self ! ProcessShow(ProcessShowData(show, processStation.processStationData))
          }
        case None =>
          log.error("could not start process")
          self ! ScheduleProcess(processStation.processStationData)
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
