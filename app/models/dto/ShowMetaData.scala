package models.dto

import java.io.File
import java.net.URL

import constants.VimeoEncodingStatusSystem.VimeoEncodingStatus
import play.api.libs.json._

/**
  * Data object for handing over data.
  *
  * @author Matthias L. Jugel
  */
case class ShowMetaData(val stationId: String, val channelId: String) {
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
  implicit object ShowMetaDataReads extends Format[ShowMetaData] {

    override def reads(json: JsValue): JsResult[ShowMetaData] = {

      val vimeoEncodingStatus = (json \ "vimeoEncodingStatus").asOpt[String]
      val vimeoJson = Json.obj("name" -> vimeoEncodingStatus, "$variant" -> vimeoEncodingStatus)

      val meta = ShowMetaData(
        (json \ "stationId").as[String],
        (json \ "channelId").as[String]
      )

      meta.hmsStationId = (json \ "hmsStationId").asOpt[String]
      meta.stationName = (json \ "stationName").asOpt[String]
      meta.stationLogoUrl = parseOptUrl(json, "stationLogoUrl")
      meta.stationLogoShow = (json \ "stationLogoShow").as[Boolean]
      meta.stationMainColor = (json \ "stationMainColor").asOpt[String]

      meta.channelName = (json \ "channelName").asOpt[String]
      meta.showTitle = (json \ "showTitle").asOpt[String]
      meta.showId = (json \ "showId").asOpt[Long]
      meta.showSubtitle = (json \ "showSubtitle").asOpt[String]
      meta.showSourceTitle = (json \ "showSourceTitle").asOpt[String]
      meta.showLogoUrl = parseOptUrl(json, "showLogoUrl")
      meta.showLength = (json \ "showLength").as[Long]
      meta.showEndInfo = (json \ "showEndInfo").asOpt[String]
      meta.rootPortalUrl = parseOptUrl(json, "rootPortalUrl")

      meta.isHD = (json \ "isHD").as[Boolean]
      meta.sourceFilename = (json \ "sourceFilename").asOpt[String]
      meta.sourceVideoUrl = parseOptUrl(json, "sourceVideoUrl")
      meta.localVideoFile = parseOptFile(json, "localVideoFile")
      meta.publicVideoUrl = parseOptUrl(json, "publicVideoUrl")

      meta.currentAccessToken = (json \ "currentAccessToken").asOpt[String]

      meta.vimeo = (json \ "vimeo").asOpt[Boolean]
      meta.vimeoDone = (json \ "vimeoDone").asOpt[Boolean]
      meta.vimeoId = (json \ "vimeoId").asOpt[Long]
      meta.vimeoEncodingStatus = vimeoJson.asOpt[VimeoEncodingStatus]

      JsSuccess(meta)

    }

    override def writes(meta: ShowMetaData): JsValue = {

      val hmsStationId = if (meta.hmsStationId.isDefined) JsString(meta.hmsStationId.get) else JsNull
      val stationName = if (meta.stationName.isDefined) JsString(meta.stationName.get) else JsNull
      val stationLogoUrl = if (meta.stationLogoUrl.isDefined) JsString(meta.stationLogoUrl.get.toString) else JsNull
      val stationMainColor = if (meta.stationMainColor.isDefined) JsString(meta.stationMainColor.get.toString) else JsNull

      val channelName = if (meta.channelName.isDefined) JsString(meta.channelName.get.toString) else JsNull
      val showTitle = if (meta.showTitle.isDefined) JsString(meta.showTitle.get.toString) else JsNull
      val showId = if (meta.showId.isDefined) JsNumber(meta.showId.get) else JsNull
      val showSubtitle = if (meta.showSubtitle.isDefined) JsString(meta.showSubtitle.get) else JsNull
      val showSourceTitle = if (meta.showSourceTitle.isDefined) JsString(meta.showSourceTitle.get) else JsNull
      val showLogoUrl = if (meta.showLogoUrl.isDefined) JsString(meta.showLogoUrl.get.toString) else JsNull
      val showEndInfo = if (meta.showEndInfo.isDefined) JsString(meta.showEndInfo.get) else JsNull
      val rootPortalUrl = if (meta.rootPortalUrl.isDefined) JsString(meta.rootPortalUrl.get.toString) else JsNull

      val sourceFilename = if (meta.sourceFilename.isDefined) JsString(meta.sourceFilename.get) else JsNull
      val sourceVideoUrl = if (meta.sourceVideoUrl.isDefined) JsString(meta.sourceVideoUrl.get.toString) else JsNull
      val localVideoFile = if (meta.localVideoFile.isDefined) JsString(meta.localVideoFile.get.getCanonicalPath) else JsNull
      val publicVideoUrl = if (meta.publicVideoUrl.isDefined) JsString(meta.publicVideoUrl.get.toString) else JsNull

      val currentAccessToken = if (meta.currentAccessToken.isDefined) JsString(meta.currentAccessToken.get) else JsNull

      val vimeo = if (meta.vimeo.isDefined) JsBoolean(meta.vimeo.get) else JsNull
      val vimeoDone = if (meta.vimeoDone.isDefined) JsBoolean(meta.vimeoDone.get) else JsNull
      val vimeoId = if (meta.vimeoId.isDefined) JsNumber(meta.vimeoId.get) else JsNull

      val vimeoEncodingStatus = if (meta.vimeoEncodingStatus.isDefined) {
        val name = meta.vimeoEncodingStatus.get.name
        Json.obj("name" -> name, "$variant" -> Some(name))
      } else JsNull

      val seq = Seq(
        "stationId" -> JsString(meta.stationId),
        "channelId" -> JsString(meta.channelId),
        "hmsStationId" -> hmsStationId,
        "stationName" -> stationName,
        "stationLogoUrl" -> stationLogoUrl,
        "stationLogoShow" -> JsBoolean(meta.stationLogoShow),
        "stationMainColor" -> stationMainColor,

        "channelName" -> channelName,
        "showTitle" -> showTitle,
        "showId" -> showId,
        "showSubtitle" -> showSubtitle,
        "showSourceTitle" -> showSourceTitle,
        "showLogoUrl" -> showLogoUrl,
        "showLength" -> JsNumber(meta.showLength),
        "showEndInfo" -> showEndInfo,
        "rootPortalUrl" -> rootPortalUrl,

        "isHD" -> JsBoolean(meta.isHD),
        "sourceFilename" -> sourceFilename,
        "sourceVideoUrl" -> sourceVideoUrl,
        "localVideoFile" -> localVideoFile,
        "publicVideoUrl" -> publicVideoUrl,

        "currentAccessToken" -> currentAccessToken,

        "vimeo" -> vimeo,
        "vimeoDone" -> vimeoDone,
        "vimeoId" -> vimeoId,
        "vimeoEncodingStatus" -> vimeoEncodingStatus
      )

      JsObject(seq)

    }

  }

  def parseOptUrl(json: JsValue, key: String): Option[URL] = {
    (json \ key).asOpt[String] match {
      case Some(urlString) => Some(new URL(urlString))
      case None => None
    }
  }

  def parseOptFile(json: JsValue, key: String): Option[File] = {
    (json \ key).asOpt[String] match {
      case Some(urlString) => Some(new File(urlString))
      case None => None
    }
  }

}

case class VideoUploadSuccess(meta: ShowMetaData)
case class VideoDownloadSuccess(meta: ShowMetaData)

case class VideoDownloadFailure(meta: ShowMetaData, e: Throwable)
case class VideoUploadFailure(meta: ShowMetaData, e: Throwable)