package actors

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.event.Logging
import com.amazonaws.auth.BasicAWSCredentials
import helper.{HMSApi, HMSShow, S3Backend, ShowMetaData}
import models.{Show, Station}
import play.api.Play
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
 * Created by dermicha on 07/09/14.
 */

case class StartProcess()

case class ProcessShow(show: HMSShow, hmsStationId: String, stationId: String, channelId: String)

case class ProcessStation(hmsStationId: String, stationId: String, channelId: String)

class ShowCrawler extends Actor {
  val log = Logging(context.system, this)

  val crawlerPeriod = Play.configuration.getInt("hms.crawler.period").get

  val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
  val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")
  val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
  val s3Backend: S3Backend = new S3Backend(credentials, Play.configuration.getString("aws.bucket").get)

  val showProcessingActor = context.actorOf(Props(new ShowProcessingActor(s3Backend)))

  //@TODO add control which stations

  def receive = {
    case processShow: ProcessShow =>
      log.info("starting show processing for: %d / %s".format(processShow.show.ID, processShow.show.Name))
      val meta = new ShowMetaData(processShow.stationId, processShow.channelId)
      meta.hmsStationId = Some(processShow.hmsStationId)
      meta.showId = Some(processShow.show.ID)
      meta.showTitle = processShow.show.Name
      //meta.sourceVideoUrl = Some(new URL(processShow.show.DownloadURL.getOrElse("").replaceAllLiterally(" ", "%20")))
      meta.sourceVideoUrl = Some(new URL(processShow.show.DownloadURL.get))
      log.info("collected meta: " + meta.showTitle + " / " + meta.sourceVideoUrl)
      showProcessingActor ! meta

    case processStation: ProcessStation =>
      log.info("try to start station processing for: %s (%s)".format(processStation.stationId, processStation.hmsStationId))
      HMSApi.getCurrentShow(processStation.hmsStationId, processStation.channelId).map {
        case Some(show) =>
          Show.findShowById(show.ID).map {
            case Some(existingShow) =>
              log.info("nothing to do for: %s / %s ".format(show.ID, show.Name))
              context.system.scheduler.scheduleOnce(
                Duration.create(crawlerPeriod, TimeUnit.MINUTES),
                self,
                new ProcessStation(processStation.hmsStationId, processStation.stationId, processStation.channelId))
            case None =>
              log.info("starting station processing for: %s (%s)".format(processStation.stationId, processStation.hmsStationId))
              self ! ProcessShow(show, processStation.hmsStationId, processStation.stationId, processStation.channelId)
          }
        case None =>
          log.error("could not start process")
          context.system.scheduler.scheduleOnce(
            Duration.create(crawlerPeriod, TimeUnit.MINUTES),
            self,
            new ProcessStation(processStation.hmsStationId, processStation.stationId, processStation.channelId))
          None
      }

    case startProcess: StartProcess =>
      log.info("starting show crawler")
      Station.allStations.map { stations =>
        log.debug("found stations: " + stations.iterator.length)
        stations.foreach { station =>
          log.debug("currently process station: " + station.stationId)
          self ! ProcessStation(station.hmsStationId, station.stationId, station.channelId)
        }
      }
  }

}
