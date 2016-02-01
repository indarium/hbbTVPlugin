package models.vimeo.video

import play.api.libs.json._

/**
  * path = /pictures
  *
  * author: cvandrei
  * since: 2016-02-01
  */
// TODO unit tests
case class Pictures(uri: String,
                    active: Boolean,
                    pictureType: String,
                    sizes: Seq[Size]
                   )

object Pictures {

  implicit object PicturesReads extends Format[Pictures] {

    override def reads(json: JsValue): JsResult[Pictures] = {

      val pictures = Pictures(
        (json \ "uri").as[String],
        (json \ "active").as[Boolean],
        (json \ "type").as[String],
        (json \ "sizes").as[Seq[Size]]
      )

      JsSuccess(pictures)

    }

    def writes(p: Pictures): JsValue = {

      JsObject(Seq(
        "uri" -> JsString(p.uri),
        "active" -> JsBoolean(p.active),
        "type" -> JsString(p.pictureType),
        "sizes" -> Json.arr(p.sizes)
      ))

    }

  }

}
