package helper

import java.io.File
import java.net.URL

import constants.VimeoEncodingStatusSystem.VimeoEncodingStatus

/**
 * Data object for handing over data.
 *
 * @author Matthias L. Jugel
 */
class ShowMetaData(val stationId: String, val channelId: String) {
  var hmsStationId: Option[String] = None
  var stationName: Option[String] = None
  var stationLogoUrl: Option[URL] = None
  var stationLogoShow: Boolean = true
  var stationMainColor: Option[String] = None

  var channelName: Option[String] = None          // vimeo: channel
  var showTitle: Option[String] = None            // vimeo: title
  var showId: Option[Long] = None
  var showSubtitle: Option[String] = None         // vimeo: description
  var showSourceTitle: Option[String] = None
  var showLogoUrl: Option[URL] = None
  var showLength: Long = 0L
  var showEndInfo: Option[String] = None
  var rootPortalUrl: Option[URL] = None

  var isHD = false
  var sourceFilename: Option[String] = None
  var sourceVideoUrl: Option[URL] = None
  var localVideoFile: Option[File] = None
  var publicVideoUrl: Option[URL] = None

  var currentAccessToken: Option[String] = None

  var vimeo: Option[Boolean] = None
  var vimeoDone: Option[Boolean] = None
  var vimeoId: Option[Long] = None
  var vimeoEncodingStatus: Option[VimeoEncodingStatus] = None

  override def toString = "showTitle: " + showTitle.getOrElse("none") + " channelName: " + channelName.getOrElse("none")

}

case class VideoUploadSuccess(meta: ShowMetaData)
case class VideoDownloadSuccess(meta: ShowMetaData)

case class VideoDownloadFailure(meta: ShowMetaData, e: Throwable)
case class VideoUploadFailure(meta: ShowMetaData, e: Throwable)
