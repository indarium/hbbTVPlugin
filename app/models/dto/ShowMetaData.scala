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
      meta.localVideoFile = parseOptFile(json,"localVideoFile")
      meta.publicVideoUrl = parseOptUrl(json, "publicVideoUrl")

      meta.currentAccessToken = (json \ "currentAccessToken").asOpt[String]

      meta.vimeo = (json \ "vimeo").asOpt[Boolean]
      meta.vimeoDone = (json \ "vimeoDone").asOpt[Boolean]
      meta.vimeoId = (json \ "vimeoId").asOpt[Long]
      meta.vimeoEncodingStatus = vimeoJson.asOpt[VimeoEncodingStatus]

      JsSuccess(meta)

    }

    override def writes(meta: ShowMetaData): JsValue = {

      var seq = Seq(
        "stationId" -> JsString(meta.stationId),
        "channelId" -> JsString(meta.channelId)
      )

      if (meta.hmsStationId.isDefined) seq ++= Seq("hmsStationId" -> JsString(meta.hmsStationId.get))
      if (meta.stationName.isDefined) seq ++= Seq("stationName" -> JsString(meta.stationName.get))
      if (meta.stationLogoUrl.isDefined) seq ++= Seq("stationLogoUrl" -> JsString(meta.stationLogoUrl.get.toString))
      seq ++= Seq("stationLogoShow" -> JsBoolean(meta.stationLogoShow))
      if (meta.stationMainColor.isDefined) seq ++= Seq("stationMainColor" -> JsString(meta.stationMainColor.get))

      if (meta.channelName.isDefined) seq ++= Seq("channelName" -> JsString(meta.channelName.get))
      if (meta.showTitle.isDefined) seq ++= Seq("showTitle" -> JsString(meta.showTitle.get))
      if (meta.showId.isDefined) seq ++= Seq("showId" -> JsNumber(meta.showId.get))
      if (meta.showSubtitle.isDefined) seq ++= Seq("showSubtitle" -> JsString(meta.showSubtitle.get))
      if (meta.showSourceTitle.isDefined) seq ++= Seq("showSourceTitle" -> JsString(meta.showSourceTitle.get))
      if (meta.showLogoUrl.isDefined) seq ++= Seq("showLogoUrl" -> JsString(meta.showLogoUrl.get.toString))
      seq ++= Seq("showLength" -> JsNumber(meta.showLength))
      if (meta.showEndInfo.isDefined) seq ++= Seq("showEndInfo" -> JsString(meta.showEndInfo.get))
      if (meta.rootPortalUrl.isDefined) seq ++= Seq("rootPortalUrl" -> JsString(meta.rootPortalUrl.get.toString))

      seq ++= Seq("isHD" -> JsBoolean(meta.isHD))
      if (meta.sourceFilename.isDefined) seq ++= Seq("sourceFilename" -> JsString(meta.sourceFilename.get))
      if (meta.sourceVideoUrl.isDefined) seq ++= Seq("sourceVideoUrl" -> JsString(meta.sourceVideoUrl.get.toString))
      if (meta.localVideoFile.isDefined) seq ++= Seq("localVideoFile" -> JsString(meta.localVideoFile.get.getCanonicalPath))
      if (meta.publicVideoUrl.isDefined) seq ++= Seq("publicVideoUrl" -> JsString(meta.publicVideoUrl.get.toString))

      if (meta.currentAccessToken.isDefined) seq ++= Seq("currentAccessToken" -> JsString(meta.currentAccessToken.get))

      if (meta.vimeo.isDefined) seq ++= Seq("vimeo" -> JsBoolean(meta.vimeo.get))
      if (meta.vimeoDone.isDefined) seq ++= Seq("vimeoDone" -> JsBoolean(meta.vimeoDone.get))
      if (meta.vimeoId.isDefined) seq ++= Seq("vimeoId" -> JsNumber(meta.vimeoId.get))
      if (meta.vimeoEncodingStatus.isDefined) seq ++= Seq("vimeoEncodingStatus" -> JsString(meta.vimeoEncodingStatus.get.name))

      JsObject(seq)

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

}

case class VideoUploadSuccess(meta: ShowMetaData)
case class VideoDownloadSuccess(meta: ShowMetaData)

case class VideoDownloadFailure(meta: ShowMetaData, e: Throwable)
case class VideoUploadFailure(meta: ShowMetaData, e: Throwable)
