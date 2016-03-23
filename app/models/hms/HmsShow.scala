package models.hms

import constants.JsonConstants
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads, Writes}

/**
  * author: cvandrei
  * since: 2016-03-23
  */
case class HmsShow(ID: Int,
                   Name: Option[String],
                   UTCEnd: DateTime,
                   DownloadURL: Option[String],
                   ChannelID: Long,
                   ParentID: Long
                  )

object HmsShow {

  implicit val dateReads = Reads.jodaDateReads(JsonConstants.dateFormat)
  implicit val dateWrites = Writes.jodaDateWrites(JsonConstants.dateFormat)

  implicit val format = Json.format[HmsShow]

}
