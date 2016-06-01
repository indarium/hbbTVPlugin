import java.io.{File, FilenameFilter}
import java.net.URL

import models.Station
import models.dto.ShowMetaData
import org.xml.sax.SAXParseException

import scala.xml.{Elem, XML}

/**
  * author: cvandrei
  * since: 2016-05-30
  */
case class MediaInfo(downloadUrl: String, source: String, name: Option[String], showId: Long)

object ImportPreparation {

  def main(args: Array[String]) {

    val dir = "/Users/cvandrei/Downloads/Backup_XML/" // TODO load from config
    val xmlFiles = listXmlFiles(dir)

    xmlFiles foreach { xmlFile =>
      val meta = toShowMetaData(dir, xmlFile)
      println(s"meta=$meta")
    }

  }

  private def listXmlFiles(dir: String): Seq[String] = {

    val backupXml = new File(dir)
    val xmlFilter = new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = {
        """.*\.xml$""".r.findFirstIn(name).isDefined
      }
    }

    backupXml.list(xmlFilter).toSeq

  }

  private def toShowMetaData(dir: String, filename: String): Option[ShowMetaData] = {

    val stationsToImport = Seq("rokTV", "FischTV") // TODO load from config

    toMediaInfo(dir, filename) match {

      case None => None

      case Some(mediaInfo) =>

        stationsToImport.contains(mediaInfo.source) match {

          case false =>
            println(s"IGNORE: source=${mediaInfo.source}")
            None

          case true =>

            println(s"mediaInfo=$mediaInfo")
            val stationOpt: Option[Station] = rokTV // TODO db lookup with ${mediaInfo.source}
            stationOpt match {

              case Some(station) => createMeta(station, mediaInfo)

              case None =>
                println(s"ERROR: station not found in db: filename=$filename, source=${mediaInfo.source}")
                None

            }

        }

    }

  }

  private def toMediaInfo(dir: String, filename: String): Option[MediaInfo] = {

    loadXml(dir, filename) match {

      case None => None

      case Some(xml) =>

        val ShowIdRegex = """(.+?)_(\d+)\.xml""".r

        val downloadUrl = (xml \ "media" \ "media").text
        val source = (xml \ "media" \ "metadatalist" \ "md_source").text

        val ShowIdRegex(nameFromUrl, showId) = filename
        val name = (xml \ "media" \ "name").text match {

          case "" => nameFromUrl match {
            case "" => None
            case s => Some(s)
          }

          case s => Some(s)

        }

        Some(MediaInfo(downloadUrl, source, name, showId.toLong))

    }

  }

  def loadXml(dir: String, filename: String): Option[Elem] = {

    try {

      val file = new File(dir + filename)
      Some(XML.load(file.toURI.toURL))

    } catch {

      case e: SAXParseException =>
        println(s"SAXParseException: filename=$filename")
        None

    }

  }

  private def createMeta(station: Station, mediaInfo: MediaInfo): Some[ShowMetaData] = {

    val meta = ShowMetaData(station.stationId, station.channelId)

    meta.hmsStationId = Some(station.hmsStationId)
    meta.showId = Some(mediaInfo.showId)
    meta.showTitle = mediaInfo.name
    meta.showSourceTitle = meta.showTitle
    meta.sourceVideoUrl = Some(new URL(mediaInfo.downloadUrl)) // TODO change url host (currently: http://62.67.13.51/htfiles/Clips/ROKTV/Zoom6_33611.mp4)

    //    if (VimeoUtil.uploadActivated(meta.stationId)) { // TODO uncomment before running program in app server
    meta.vimeo = Some(true)
    //    }

    Some(meta)

  }

  private def rokTV: Option[Station] = {
    Some(
      Station(stationId = "rokTV",
        hmsStationId = "rok-TV",
        channelId = "rok-TV",
        active = true,
        defaultStationName = "rok-TV",
        defaultStationLogoUrl = "http://hbbtvplugin.indarium.de/logos/ltv_logo_v3.png",
        defaultStationLogoDisplay = false,
        defaultStationMainColor = "#262c9e",
        defaultChannelName = "rok-TV",
        defaultShowTitle = "rok-TV Journal",
        defaultShowSubtitle = "Lokale Informationen aus Rostock.",
        defaultShowLogoUrl = "http://hbbtvplugin.indarium.de/logos/ltv_logo_v3.png",
        defaultChannelBroadcastInfo = "<h2>Sendetermine</h2><table><tr><td>Montag bis Freitag</td><td>18:00 Uhr</td></tr><tr><td>Samstag</td><td>21:00 Uhr</td></table>",
        defaultRootPortalURL = "http://application.ses-ps.com/Senderportal-BB-MV/index.html",
        getShowUrlPattern = Some("/Show/{HMS-STATION-ID}?&Order=DESC&Count=15"),
        keepLastShows = None,
        hmsEncodingProfile = Some("HDTV_WEB_HIGH_P25")
      )
    )
  }

}
