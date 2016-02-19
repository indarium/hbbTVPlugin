package models.hms

import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-19
  */
case class TranscodeCallback(ID: Long,
                             VerboseMessage: String,
                             Status: String,
                             StatusValue: Option[Int],
                             StatusUnit: Option[String],
                             DownloadSource: Option[String])

object TranscodeCallback {
  implicit val reads = Json.reads[TranscodeCallback]
  implicit val writes = Json.writes[TranscodeCallback]
}
