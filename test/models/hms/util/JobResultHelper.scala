package models.hms.util

import models.hms.JobResult

/**
  * author: cvandrei
  * since: 2016-02-18
  */
object JobResultHelper {

  val DEFAULT_ID = -1L
  val DEFAULT_RESULT = resultValue(DEFAULT_ID)
  val DEFAULT_VERBOSE_RESULT = verboseResultValue(DEFAULT_ID)

  def defaultObject: JobResult = JobResult(DEFAULT_ID, DEFAULT_RESULT, DEFAULT_VERBOSE_RESULT, None)

  def defaultObject(id: Long): JobResult = JobResult(id, resultValue(id), verboseResultValue(id), None)

  def defaultJson: String =
    s"""
       |{
       |  "ID": $DEFAULT_ID,
       |  "Result": "$DEFAULT_RESULT",
       |  "VerboseResult": "$DEFAULT_VERBOSE_RESULT"
       |}
     """.stripMargin

  private def resultValue(id: Long): String = s"result-$id"

  private def verboseResultValue(id: Long): String = s"verboseResult-$id"

}
