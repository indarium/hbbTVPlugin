package models.hms

import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-18
  */
case class TranscodeResponse(Jobs: List[JobResult])

object TranscodeResponse {
  implicit val reads = Json.reads[TranscodeResponse]
  implicit val writes = Json.writes[TranscodeResponse]
}
