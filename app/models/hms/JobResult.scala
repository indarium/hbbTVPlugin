package models.hms

import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-18
  */
case class JobResult(ID: Long,
                     Result: String,
                     VerboseResult: String)

object JobResult {
  implicit val reads = Json.reads[JobResult]
  implicit val writes = Json.writes[JobResult]
}
