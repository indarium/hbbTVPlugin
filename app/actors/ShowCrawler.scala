package actors

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.event.Logging
import com.amazonaws.auth.BasicAWSCredentials
import helper._
import models.{Show, Station}
import play.api.Play
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Random

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

  val crawlerPeriod = Play.configuration.getInt("hms.crawler.period").get

  val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
  val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")
  val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
  val s3Backend: S3Backend = new S3Backend(credentials, Play.configuration.getString("aws.bucket").get)

  val vimeoAccessToken: String = Play.configuration.getString("vimeo.accessToken").getOrElse("NO-ACCESS-TOKEN")
  val vimeoBackend: VimeoBackend = new VimeoBackend(vimeoAccessToken)

  val showProcessingActor = context.actorOf(Props(new ShowProcessingActor(s3Backend)))

  //@TODO add control which stations

  def receive = {
    case processShow: ProcessShow =>
      log.info("starting show processing for: %d / %s".format(processShow.processShowData.show.ID, processShow.processShowData.show.Name))
      val meta = new ShowMetaData(processShow.processShowData.processStationData.stationId, processShow.processShowData.processStationData.channelId)
      meta.hmsStationId = Some(processShow.processShowData.processStationData.hmsStationId)
      meta.showId = Some(processShow.processShowData.show.ID)
      meta.showTitle = processShow.processShowData.show.Name
      //meta.sourceVideoUrl = Some(new URL(processShow.show.DownloadURL.getOrElse("").replaceAllLiterally(" ", "%20")))
      meta.sourceVideoUrl = Some(new URL(processShow.processShowData.show.DownloadURL.get))
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
      Station.allStations.map { stations =>
        log.debug("found stations: " + stations.iterator.length)
        var count:Int = 0
        stations.foreach { station =>
          count += 10
          log.info("will launch processing of station: %s in %d seconds ".format(station.stationId, count))
          context.system.scheduler.scheduleOnce(
            Duration.create(count, TimeUnit.SECONDS),
            self,
            ProcessStation(ProcessStationData(station.hmsStationId, station.stationId, station.channelId)))
        }
      }

    case scheduleProcess: ScheduleProcess =>
      log.info("scheduling show crawler")
      context.system.scheduler.scheduleOnce(
        Duration.create(crawlerPeriod, TimeUnit.MINUTES),
        self,
        ProcessStation(scheduleProcess.processStationData))
  }

}
