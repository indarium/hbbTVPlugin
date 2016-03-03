package models.hms.util

import models.hms.TranscodeCallback

/**
  * author: cvandrei
  * since: 2016-02-19
  */
object TranscodeCallbackHelper {

  def queuedObject(id: Long): TranscodeCallback = TranscodeCallback(None, id, "created transcode job", "queued", None, None, None)

  def processingObject(id: Long): TranscodeCallback = TranscodeCallback(None, id, verboseMessage(id), "processing", Some(80), Some("percentage"), None)

  def finishedObject(id: Long): TranscodeCallback = TranscodeCallback(None, id, verboseMessage(id), "finished", None, None, downloadSource(id))

  def queuedJson(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "created transcode job",
       |  "Status": "queued"
       |}
     """.stripMargin

  def processingJson(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "${verboseMessage(id)}",
       |  "Status": "processing",
       |  "StatusValue": 80,
       |  "StatusUnit": "percentage"
       |}
     """.stripMargin

  def finishedJson(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "{verboseMessage(id)}",
       |  "Status": "finished",
       |  "DownloadSource": "${downloadSource(id)}"
       |}
     """.stripMargin

  private def verboseMessage(id: Long): String = s"verboseMessage-$id"

  private def downloadSource(id: Long): Option[String] = Some(s"http://hmsservicefarm/hmsWSTranscode/api/customers/download/result-$id.mp4")

}