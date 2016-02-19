package models.hms

import models.dto.ShowMetaData
import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-18
  */
case class JobResult(ID: Long,
                     Result: String,
                     VerboseResult: String,
                     meta: Option[ShowMetaData])

object JobResult {
  // TODO update tests to account for ShowMetaData which we just added
  implicit val reads = Json.reads[JobResult]
  implicit val writes = Json.writes[JobResult]
}
