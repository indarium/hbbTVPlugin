package models.hms

import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-17
  */
case class Overlay(ID: Long,
                   StartOffset: Option[String],
                   EndOffset: Option[String],
                   Layer: Int
                  )

object Overlay {
  implicit val reads = Json.reads[Overlay]
  implicit val writes = Json.writes[Overlay]
}
