package models.hms

import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-17
  */
case class Source(ID: Long,
                  SourceName: Option[String],
                  StartOffset: Option[String],
                  EndOffset: Option[String],
                  DestinationName: String,
                  Overlays: Option[List[Overlay]],
                  Profile: String // TODO use enum?
                 )

object Source {
  implicit val reads = Json.reads[Source]
  implicit val writes = Json.writes[Source]
}
