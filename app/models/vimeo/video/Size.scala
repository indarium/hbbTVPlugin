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

  implicit val reads = Json.reads[Size]
  implicit val writes = Json.writes[Size]

}
