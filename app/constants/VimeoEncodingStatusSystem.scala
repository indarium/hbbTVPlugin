package constants

import play.api.libs.json._

/**
  * author: pereira
  * since: 2016-01-27
  */
object VimeoEncodingStatusSystem {

  sealed trait VimeoEncodingStatus {
    val vimeoEncodingStatus: String
  }

  case object IN_PROGRESS extends VimeoEncodingStatus {
    override val vimeoEncodingStatus: String = "IN_PROGRESS"
  }
  case object DONE extends VimeoEncodingStatus {
    override val vimeoEncodingStatus: String = "DONE"
  }

  implicit def vimeoStatusEncodingReads[T](implicit fmt: Reads[T]): Reads[VimeoEncodingStatus] = new Reads[VimeoEncodingStatus] {
    def reads(json: JsValue): VimeoEncodingStatus = new VimeoEncodingStatus (
      (json \ "vimeo-encoding-status").as[String]
    )
  }

  implicit def vimeoStatusEncodingWrites[T](implicit fmt: Writes[T]): Writes[VimeoEncodingStatus] = new Writes[VimeoEncodingStatus] {
    def writes(vimeoEncodingStatus: VimeoEncodingStatus) = JsObject(Seq(
      "vimeo-encoding-status" -> JsString(vimeoEncodingStatus.vimeoEncodingStatus)
    ))
  }

//  implicit val reads: Reads[VimeoEncodingStatus] = Variants.reads[VimeoEncodingStatus]
//  implicit val writes: Writes[VimeoEncodingStatus] = Variants.writes[VimeoEncodingStatus]

}