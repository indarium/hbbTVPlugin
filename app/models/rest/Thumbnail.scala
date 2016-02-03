package models.rest

import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-03
  */
case class Thumbnail(width: Int,
                     height: Int,
                     url: String
                    )

object Thumbnail {
  implicit val reads = Json.reads[Thumbnail]
  implicit val writes = Json.writes[Thumbnail]
}
