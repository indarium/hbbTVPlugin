package models.hms

import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-17
  */
case class Transcode(SourceType: String, // TODO use enum
                     Sources: List[Source],
                     Collapsed: Option[Boolean],
                     CollapsedName: Option[String],
                     DownloadProvision: String, // TODO use enum
                     PushFinishedNotification: Boolean,
                     PushErrorNotification: Boolean,
                     PushStatusNotification: Boolean,
                     PushNotificationCallback: String
                    )

object Transcode {
  implicit val reads = Json.reads[Transcode]
  implicit val writes = Json.writes[Transcode]
}
