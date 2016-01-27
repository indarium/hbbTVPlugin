package constants

/**
  * author: cvandrei
  * since: 2016-01-26
  */
/** sealed class */
sealed class VimeoEncodingStatus(val name: String)
case class IN_PROGRESS() extends VimeoEncodingStatus("IN_PROGRESS")
case class DONE() extends VimeoEncodingStatus("DONE")

/** scala enumeration */
//object VimeoEncodingStatus extends Enumeration {
//  val IN_PROGRESS = Value("IN_PROGRESS")
//  val DONE = Value("DONE")
//}

/** scala object as enumeration */
//object VimeoEncodingStatus extends Enumeration {
//  type VimeoEncodingStatus = Value
//  val IN_PROGRESS, DONE = Value
//}

/** sealed trait */
//sealed trait VimeoEncodingStatus { def name: String }
//case object IN_PROGRESS extends VimeoEncodingStatus { val name = "IN_PROGRESS" }
//case object DONE extends VimeoEncodingStatus { val name = "DONE" }

/** sealed trait extending enum trait */
//trait Enum[A] {
//  trait Value {self: A => }
//  val values: List[A]
//}

//sealed trait VimeoEncodingStatus extends VimeoEncodingStatus.Value
//object VimeoEncodingStatus extends Enum[VimeoEncodingStatus] {
//  case object IN_PROGRESS extends VimeoEncodingStatus
//  case object DONE extends VimeoEncodingStatus
//  val value = List(IN_PROGRESS, DONE)
//}

//sealed trait VimeoEncodingStatus extends VimeoEncodingStatus.Value
//object VimeoEncodingStatus extends Enum[VimeoEncodingStatus] {
//  case object IN_PROGRESS extends VimeoEncodingStatus
//  case object DONE extends VimeoEncodingStatus
//  val values = List(IN_PROGRESS, DONE)
//}

//sealed abstract class VimeoEncodingStatus(val name: String)
//case object IN_PROGRESS extends VimeoEncodingStatus("IN_PROGRESS")
//case object DONE extends VimeoEncodingStatus("DONE")
