package constants

import julienrf.variants.Variants
import play.api.libs.json._

/**
  * author: pereira
  * since: 2016-01-27
  */
object VimeoEncodingStatusSystem {

  sealed trait VimeoEncodingStatus {
    val name: String
  }

  case object IN_PROGRESS extends VimeoEncodingStatus {
    override val name: String = "IN_PROGRESS"
  }
  case object DONE extends VimeoEncodingStatus {
    override val name: String = "DONE"
  }

  implicit val reads: Reads[VimeoEncodingStatus] = Variants.reads[VimeoEncodingStatus]
  implicit val writes: Writes[VimeoEncodingStatus] = Variants.writes[VimeoEncodingStatus]
}