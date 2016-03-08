package models.hms

import play.api.libs.json._

/**
  * author: cvandrei
  * since: 2016-02-17
  */
case class Transcode(SourceType: String,
                     Sources: List[Source],
                     Collapsed: Option[Boolean],
                     CollapsedName: Option[String],
                     DownloadProvision: String,
                     PushFinishedNotification: Boolean,
                     PushErrorNotification: Boolean,
                     PushStatusNotification: Boolean,
                     PushNotificationCallback: String
                    )

object Transcode {

  implicit object TranscodeReads extends Format[Transcode] {

    override def reads(json: JsValue): JsResult[Transcode] = {

      val collapsed = (json \ "Collapsed").asOpt[String] match {
        case Some("true") => Some(true)
        case Some("false") => Some(false)
        case _ => None
      }

      val transcode = Transcode(
        (json \ "SourceType").as[String],
        (json \ "Sources").as[List[Source]],
        collapsed,
        (json \ "CollapsedName").asOpt[String],
        (json \ "DownloadProvision").as[String],
        (json \ "PushFinishedNotification").as[String].toBoolean,
        (json \ "PushErrorNotification").as[String].toBoolean,
        (json \ "PushStatusNotification").as[String].toBoolean,
        (json \ "PushNotificationCallback").as[String]
      )
      JsSuccess(transcode)

    }

    override def writes(t: Transcode): JsValue = {

      val collapsed: Option[String] = if (t.Collapsed.isDefined) Some(t.Collapsed.get.toString) else None

      Json.obj(
        "SourceType" -> t.SourceType,
        "Sources" -> t.Sources,
        "Collapsed" -> collapsed,
        "CollapsedName" -> t.CollapsedName,
        "DownloadProvision" -> t.DownloadProvision,
        "PushFinishedNotification" -> t.PushFinishedNotification.toString,
        "PushErrorNotification" -> t.PushErrorNotification.toString,
        "PushStatusNotification" -> t.PushStatusNotification.toString,
        "PushNotificationCallback" -> t.PushNotificationCallback
      )

    }

  }

}
