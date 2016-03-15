package models.hms.util

import models.hms.TranscodeResponse
import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-18
  */
object TranscodeResponseHelper {

  private val jobResult1 = JobResultHelper.defaultObject(-1L)
  private val jobResult2 = JobResultHelper.defaultObject(-2L)
  private val list = List(jobResult1, jobResult2)

  def defaultObject: TranscodeResponse = TranscodeResponse(list)

  def defaultJson: String = {

    val jobResult1Json = Json.toJson(jobResult1)
    val jobResult2Json = Json.toJson(jobResult2)

    s"""
       |{
       |  "Jobs": [
       |    $jobResult1Json,
       |    $jobResult2Json
       |  ]
       |}
     """.stripMargin

  }

}
