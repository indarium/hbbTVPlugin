package models.vimeo.video

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

  import play.api.libs.json._

  implicit val reads = Json.reads[Size]
  implicit val writes = Json.writes[Size]
}
