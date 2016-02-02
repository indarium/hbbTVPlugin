package models.vimeo.video

import play.api.libs.json._

/**
  * path = /download/{[*]}
  *
  * author: cvandrei
  * since: 2016-02-01
  */
case class Download(quality: String,
                    fileType: String,
                    width: Int,
                    height: Int,
                    expires: String, // TODO refactor to date
                    link: String,
                    createdTime: String, // TODO refactor to date
                    fps: Int,
                    size: Long,
                    md5: String
                   )

object Download {

  implicit object downloadReads extends Format[Download] {

    override def reads(json: JsValue): JsResult[Download] = {

      val download = Download(
        (json \ "quality").as[String],
        (json \ "type").as[String],
        (json \ "width").as[Int],
        (json \ "height").as[Int],
        (json \ "expires").as[String],
        (json \ "link").as[String],
        (json \ "created_time").as[String],
        (json \ "fps").as[Int],
        (json \ "size").as[Int],
        (json \ "md5").as[String]
      )

      JsSuccess(download)

    }

    def writes(d: Download) = {

      JsObject(Seq(
        "quality" -> JsString(d.quality),
        "type" -> JsString(d.fileType),
        "width" -> JsNumber(d.width),
        "height" -> JsNumber(d.height),
        "expires" -> JsString(d.expires),
        "link" -> JsString(d.link),
        "created_time" -> JsString(d.createdTime),
        "fps" -> JsNumber(d.fps),
        "size" -> JsNumber(d.size),
        "md5" -> JsString(d.md5)
      ))

    }

  }

}
