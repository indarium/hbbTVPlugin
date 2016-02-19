package models.dto

import java.io.File
import java.net.URL

import constants.VimeoEncodingStatusSystem.VimeoEncodingStatus
import play.api.libs.json.Json

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

object ShowMetaData {

  // TODO unit test
  implicit val reads = Json.reads[ShowMetaData]
  implicit val writes = Json.writes[ShowMetaData]

  def apply(stationId: String,
            channelId: String,
            hmsStationId: Option[String],
            stationName: Option[String],
            stationLogoUrl: Option[URL],
            stationLogoShow: Boolean,
            stationMainColor: Option[String],
            channelName: Option[String],
            showTitle: Option[String],
            showId: Option[Long],
            showSubtitle: Option[String],
            showSourceTitle: Option[String],
            showLogoUrl: Option[URL],
            showLength: Long,
            showEndInfo: Option[String],
            rootPortalUrl: Option[URL],
            isHD: Boolean,
            sourceFilename: Option[String],
            sourceVideoUrl: Option[URL],
            localVideoFile: Option[File],
            publicVideoUrl: Option[URL],
            currentAccessToken: Option[String]
//            vimeo: Option[Boolean],
//            vimeoDone: Option[Boolean],
//            vimeoId: Option[Long],
//            vimeoEncodingStatus: Option[VimeoEncodingStatus]
           ) = {

    val meta = new ShowMetaData(stationId, channelId)

    meta.hmsStationId = hmsStationId
    meta.stationName = stationName
    meta.stationLogoUrl = stationLogoUrl
    meta.stationLogoShow = stationLogoShow
    meta.stationMainColor = stationMainColor

    meta.channelName = channelName
    meta.showTitle = showTitle
    meta.showId = showId
    meta.showSubtitle = showSubtitle
    meta.showSourceTitle = showSourceTitle
    meta.showLogoUrl = showLogoUrl
    meta.showLength = -showLength
    meta.showEndInfo = showEndInfo
    meta.rootPortalUrl = rootPortalUrl

    meta.isHD = isHD
    meta.sourceFilename = sourceFilename
    meta.sourceVideoUrl = sourceVideoUrl
    meta.localVideoFile = localVideoFile
    meta.publicVideoUrl = publicVideoUrl

    meta.currentAccessToken = currentAccessToken

//    meta.vimeo = vimeo
//    meta.vimeoDone = vimeoDone
//    meta.vimeoId = vimeoId
//    meta.vimeoEncodingStatus = vimeoEncodingStatus

    meta

  }

  def unapply(meta: ShowMetaData) = {

    if (meta == null) None
    else Some(
      meta.stationId,
      meta.channelId,
      meta.hmsStationId,
      meta.stationName,
      meta.stationLogoUrl,
      meta.stationLogoShow,
      meta.stationMainColor,
      meta.channelName,
      meta.showTitle,
      meta.showId,
      meta.showSubtitle,
      meta.showSourceTitle,
      meta.showLogoUrl,
      meta.showLength,
      meta.showEndInfo,
      meta.rootPortalUrl,
      meta.isHD,
      meta.sourceFilename,
      meta.sourceVideoUrl,
      meta.localVideoFile,
      meta.publicVideoUrl,
      meta.currentAccessToken
      // TODO Some() is limited in the number parameters it accepts
//            meta.vimeo,
      //      meta.vimeoDone,
      //      meta.vimeoId,
      //      meta.vimeoEncodingStatus
    )

  }

}

case class VideoUploadSuccess(meta: ShowMetaData)
case class VideoDownloadSuccess(meta: ShowMetaData)

case class VideoDownloadFailure(meta: ShowMetaData, e: Throwable)
case class VideoUploadFailure(meta: ShowMetaData, e: Throwable)
