import java.io.{File, FilenameFilter}

import models.Station
import models.dto.ShowMetaData
import org.xml.sax.SAXParseException

import scala.xml.XML

/**
  * author: cvandrei
  * since: 2016-05-30
  */
object ImportPreparation {

  def main(args: Array[String]) {

    val dir = "/Users/cvandrei/Downloads/Backup_XML/"
    val xmlFiles = listXmlFiles(dir)
    //    xmlFiles foreach (println(_))

    //    val first = xmlFiles.head
    //    toShowMetaData(dir, first)
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

    val stationsToImport = Seq("rokTV", "FischTV")
    val ShowIdRegex = """.+?(\d+)\.xml""".r
    val file = new File(dir + filename)
    try {

      val xml = XML.load(file.toURI.toURL)

      val media = (xml \ "media" \ "media").text
      val source = (xml \ "media" \ "metadatalist" \ "md_source").text
      val ShowIdRegex(showId) = filename

      stationsToImport.contains(source) match {

        case true =>
          println(s"media=$media, source=$source, showId=$showId")
          val stationOpt: Option[Station] = None // TODO db lookup with $source
          stationOpt match {

            case Some(station) =>
              val meta = ShowMetaData(station.stationId, station.channelId)
              // TODO set hmsStationId
              meta.showId = Some(showId.toLong)
              // TODO set showTitle
              // TODO set showSourceTitle (=showTitle)
              // TODO set vimeo (if Vimeo upload activated)
              // TODO set sourceVideoUrl
              Some(meta)

            case None =>
              println(s"ERROR: station not found in db: filename=$filename, source=$source")
              None

          }

        case false =>
          println(s"IGNORE: source=$source")
          None

      }

    } catch {
      case e: SAXParseException =>
        println(s"SAXParseException: filename=$filename")
        None
    }

  }

}
