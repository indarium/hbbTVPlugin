package models.vimeo.video

/**
  * path = /pictures
  *
  * author: cvandrei
  * since: 2016-02-01
  */
case class Pictures(uri: String,
                    active: Boolean,
                    pictureType: String,
                    sizes: List[Size]
                   )

object Pictures {

  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit object PicturesReads extends Format[Pictures] {

    override def reads(json: JsValue): JsResult[Pictures] = {

      val pictures = Pictures(
        (json \ "uri").as[String],
        (json \ "active").as[Boolean],
        (json \ "type").as[String],
        (json \ "sizes").as[List[Size]]
      )
      JsSuccess(pictures)
    }

    def writes(p: Pictures): JsValue = {
      Json.obj(
        "uri" -> p.uri,
        "active" -> p.active,
        "type" -> p.pictureType,
        "sizes" -> p.sizes
      )
    }
  }
}