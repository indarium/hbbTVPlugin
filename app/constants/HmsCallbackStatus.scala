package constants

/**
  * callback status types as documented at
  * http://62.67.13.57/confluence/display/DEV/hmsWSTranscode+-+transcode#hmsWSTranscode-transcode-Statustypen
  *
  * Created by cvandrei on 2016-03-07.
  */
object HmsCallbackStatus {

  val QUEUED = "Queued"
  val PREPARE = "Prepared"
  val PROCESSING = "Processing"
  val FINISHED = "Finished"
  val FAULTY = "Faulty"

}
