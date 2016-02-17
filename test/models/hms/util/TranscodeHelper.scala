package models.hms.util

import models.hms.Transcode
import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-17
  */
object TranscodeHelper {

  val DEFAULT_SOURCE_TYPE = "Media"
  val DEFAULT_SOURCES = SourceHelper.defaultList
  val DEFAULT_COLLAPSED = true
  val DEFAULT_COLLAPSED_NAME = "collapsedName"
  val DEFAULT_DOWNLOAD_PROVISION = "HTTP"
  val DEFAULT_PUSH_FINISHED_NOTIFICATION = true
  val DEFAULT_PUSH_ERROR_NOTIFICATION = true
  val DEFAULT_PUSH_STATUS_NOTIFICATION = true
  val DEFAULT_PUSH_NOTIFICATION_CALLBACK = "http://server/path"

  def default: Transcode = Transcode(DEFAULT_SOURCE_TYPE,
    DEFAULT_SOURCES,
    Some(DEFAULT_COLLAPSED),
    Some(DEFAULT_COLLAPSED_NAME),
    DEFAULT_DOWNLOAD_PROVISION,
    DEFAULT_PUSH_FINISHED_NOTIFICATION,
    DEFAULT_PUSH_ERROR_NOTIFICATION,
    DEFAULT_PUSH_STATUS_NOTIFICATION,
    DEFAULT_PUSH_NOTIFICATION_CALLBACK)

  def defaultMinimum: Transcode = Transcode(DEFAULT_SOURCE_TYPE,
    DEFAULT_SOURCES,
    None,
    None,
    DEFAULT_DOWNLOAD_PROVISION,
    DEFAULT_PUSH_FINISHED_NOTIFICATION,
    DEFAULT_PUSH_ERROR_NOTIFICATION,
    DEFAULT_PUSH_STATUS_NOTIFICATION,
    DEFAULT_PUSH_NOTIFICATION_CALLBACK)

  def defaultJson: String = {

    val sources = Json.toJson(SourceHelper.defaultList)

    s"""{
        |  "SourceType": "$DEFAULT_SOURCE_TYPE",
        |  "Sources": $sources,
        |  "Collapsed": $DEFAULT_COLLAPSED,
        |  "CollapsedName": "$DEFAULT_COLLAPSED_NAME",
        |  "DownloadProvision": "$DEFAULT_DOWNLOAD_PROVISION",
        |  "PushFinishedNotification": $DEFAULT_PUSH_FINISHED_NOTIFICATION,
        |  "PushErrorNotification": $DEFAULT_PUSH_ERROR_NOTIFICATION,
        |  "PushStatusNotification": $DEFAULT_PUSH_STATUS_NOTIFICATION,
        |  "PushNotificationCallback": "$DEFAULT_PUSH_NOTIFICATION_CALLBACK"
        |}""".
      stripMargin

  }

  def defaultJsonMinimum: String = {

    val sources = Json.toJson(SourceHelper.defaultList)

    s"""{
        |  "SourceType": "$DEFAULT_SOURCE_TYPE",
        |  "Sources": $sources,
        |  "DownloadProvision": "$DEFAULT_DOWNLOAD_PROVISION",
        |  "PushFinishedNotification": $DEFAULT_PUSH_FINISHED_NOTIFICATION,
        |  "PushErrorNotification": $DEFAULT_PUSH_ERROR_NOTIFICATION,
        |  "PushStatusNotification": $DEFAULT_PUSH_STATUS_NOTIFICATION,
        |  "PushNotificationCallback": "$DEFAULT_PUSH_NOTIFICATION_CALLBACK"
        |}""".
      stripMargin

  }

}
