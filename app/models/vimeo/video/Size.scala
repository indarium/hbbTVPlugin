package models.vimeo.video

import play.api.libs.json._

/**
  * path = /pictures/sizes{[*]}
  *
  * author: cvandrei
  * since: 2016-02-01
  */
case class Size(width: Int,
                height: Int,
                link: String
               )

object Size {

  implicit object SizeReads extends Format[Size] {

    override def reads(json: JsValue): JsResult[Size] = {

      val size = Size(
        (json \ "width").as[Int],
        (json \ "height").as[Int],
        (json \ "link").as[String]
      )

      JsSuccess(size)

    }

    override def writes(s: Size): JsValue = {

      JsObject(Seq(
        "width" -> JsNumber(s.width),
        "height" -> JsNumber(s.height),
        "link" -> JsString(s.link)
      ))

    }

  }

}
