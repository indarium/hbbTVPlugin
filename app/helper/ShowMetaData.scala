package helper

import java.io.File
import java.net.URL

import akka.actor.Status
import akka.actor.Status.{Status, Failure}

/**
 * Data object for handing over data.
 *
 * @author Matthias L. Jugel
 */
class ShowMetaData(val stationId: String, val channelId: String) {
  var stationName: Option[String] = None
  var stationLogoUrl: Option[URL] = None
  var stationLogoShow: Boolean = true
  var stationMainColor: Option[String] = None

  var channelName: Option[String] = None
  var showTitle: Option[String] = None
  var showId: Option[String] = None
  var showSubtitle: Option[String] = None
  var showLogoUrl: Option[URL] = None
  var showLength: Long = 0L
  var showEndInfo: Option[String] = None
  var rootPortalUrl: Option[URL] = None

  var isHD = false
  var sourceVideoUrl: Option[URL] = None
  var localVideoFile: Option[File] = None
  var publicVideoUrl: Option[URL] = None
}

case class VideoUploadSuccess(meta: ShowMetaData)
case class VideoDownloadSuccess(meta: ShowMetaData)

case class VideoDownloadFailure(meta: ShowMetaData, e: Throwable)
case class VideoUploadFailure(meta: ShowMetaData, e: Throwable)
