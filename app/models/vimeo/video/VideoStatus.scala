package models.vimeo.video

import play.api.libs.json._

/**
  * path = /
  *
  * Only selected parts of the JSON structure are parsed.
  *
  * author: cvandrei
  * since: 2016-02-01
  */
case class VideoStatus(pictures: Pictures,
                       download: Seq[Download],
                       files: Seq[File]
                      )

object VideoStatus {

  implicit object VideoStatusReads extends Format[VideoStatus] {

    override def reads(json: JsValue): JsResult[VideoStatus] = {

      val videoStatus = VideoStatus(
        (json \ "pictures").as[Pictures],
        (json \ "download").as[Seq[Download]],
        (json \ "files").as[Seq[File]]
      )

      JsSuccess(videoStatus)

    }

    def writes(vs: VideoStatus): JsValue = {

      JsObject(Seq(
//        "pictures" -> JsObject.(vs.pictures),
        "download" -> Json.arr(vs.download),
        "files" -> Json.arr(vs.files)
      ))

    }

  }

}
