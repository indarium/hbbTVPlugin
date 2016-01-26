package constants

/**
  * author: cvandrei
  * since: 2016-01-26
  */
sealed abstract class VimeoEncodingStatus(val name: String)

case object IN_PROGRESS extends VimeoEncodingStatus("IN_PROGRESS")
case object DONE extends VimeoEncodingStatus("DONE")
