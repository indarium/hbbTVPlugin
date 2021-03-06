package models.vimeo.video

import play.api.libs.json._

/**
  * path = /files/[*]
  *
  * author: cvandrei
  * since: 2016-02-01
  */
case class File(quality: String,
                fileType: String,
                width: Option[Int],
                height: Option[Int],
                link: String,
                linkSecure: String,
                createdTime: String, // TODO refactor to date
                fps: Int,
                size: Long,
                md5: String
               )

object File {

  implicit object FileReads extends Format[File] {

    override def reads(json: JsValue): JsResult[File] = {

      val file = File(
        (json \ "quality").as[String],
        (json \ "type").as[String],
        (json \ "width").asOpt[Int],
        (json \ "height").asOpt[Int],
        (json \ "link").as[String],
        (json \ "link_secure").as[String],
        (json \ "created_time").as[String], // TODO refactor to date
        (json \ "fps").as[Int],
        (json \ "size").as[Long],
        (json \ "md5").as[String]
      )

      JsSuccess(file)

    }

    override def writes(f: File): JsValue = {

      val width: JsValue = if (f.width.isEmpty) JsNull else JsNumber(f.width.get)
      val height: JsValue = if (f.height.isEmpty) JsNull else JsNumber(f.height.get)

      JsObject(Seq(
        "quality" -> JsString(f.quality),
        "type" -> JsString(f.fileType),
        "width" -> width,
        "height" -> height,
        "link" -> JsString(f.link),
        "link_secure" -> JsString(f.linkSecure),
        "created_time" -> JsString(f.createdTime),
        "fps" -> JsNumber(f.fps),
        "size" -> JsNumber(f.size),
        "md5" -> JsString(f.md5)
      ))

    }

  }

}
