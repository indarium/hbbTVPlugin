package models.hms

import play.api.libs.json._

/**
  * author: cvandrei
  * since: 2016-02-18
  */
case class JobResult(ID: Long,
                     Result: String,
                     VerboseResult: String
                     )

object JobResult {

  implicit object JobResultReads extends Format[JobResult] {

    override def reads(json: JsValue): JsResult[JobResult] = {

      val result = (json \ "Result").as[String]
      val id = result.toLong

      val jobResult = JobResult(
        id,
        (json \ "Result").as[String],
        (json \ "VerboseResult").as[String]
      )

      JsSuccess(jobResult)

    }

    override def writes(jr: JobResult): JsValue = {

      Json.obj(
        "Result" -> jr.Result,
        "VerboseResult" -> jr.VerboseResult
      )

    }

  }

}
