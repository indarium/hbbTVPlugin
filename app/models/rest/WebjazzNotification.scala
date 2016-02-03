package models.rest

import play.api.libs.json._

/**
  * author: cvandrei
  * since: 2016-02-03
  */
case class WebjazzNotification(auth: String,
                               vimeoId: Long,
                               hmsId: Long,
                               width: Int,
                               height: Int,
                               thumbnails: List[Thumbnail])

object WebjazzNotification {

  implicit object WebjazzNotificationReads extends Format[WebjazzNotification] {

    override def reads(json: JsValue): JsResult[WebjazzNotification] = {

      val webjazzNotification = WebjazzNotification(
        (json \ "auth").as[String],
        (json \ "vimeo-id").as[Long],
        (json \ "hms-id").as[Long],
        (json \ "width").as[Int],
        (json \ "height").as[Int],
        (json \ "thumbnails").as[List[Thumbnail]]
      )

      JsSuccess(webjazzNotification)

    }

    def writes(wn: WebjazzNotification): JsValue = {
      Json.obj(
        "auth" -> wn.auth,
        "vimeo-id" -> wn.vimeoId,
        "hms-id" -> wn.hmsId,
        "width" -> wn.width,
        "height" -> wn.height,
        "thumbnails" -> wn.thumbnails
      )
    }

  }

}
