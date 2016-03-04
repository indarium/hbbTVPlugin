package models.hms.util

import models.dto.util.ShowMetaDataHelper
import models.hms.TranscodeCallback

/**
  * author: cvandrei
  * since: 2016-02-19
  */
object TranscodeCallbackHelper {

  val DEFAULT_META = Some(ShowMetaDataHelper.defaultObject("MV1", "SAT", -1001L))

  def queuedObjectWithoutMeta(id: Long): TranscodeCallback = TranscodeCallback(None, id, "created transcode job", "queued", None, None, None, None)

  def queuedObjectWithMeta(id: Long): TranscodeCallback = TranscodeCallback(None, id, "created transcode job", "queued", None, None, None, DEFAULT_META)

  def processingObjectWithoutMeta(id: Long): TranscodeCallback = TranscodeCallback(None, id, verboseMessage(id), "processing", Some(80), Some("percentage"), None, None)

  def processingObjectWithMeta(id: Long): TranscodeCallback = TranscodeCallback(None, id, verboseMessage(id), "processing", Some(80), Some("percentage"), None, DEFAULT_META)

  def finishedObjectWithoutMeta(id: Long): TranscodeCallback = TranscodeCallback(None, id, verboseMessage(id), "finished", None, None, downloadSource(id), None)

  def finishedObjectWithMeta(id: Long): TranscodeCallback = TranscodeCallback(None, id, verboseMessage(id), "finished", None, None, downloadSource(id), DEFAULT_META)

  def queuedJsonWithoutMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "created transcode job",
       |  "Status": "queued"
       |}
     """.stripMargin

  def queuedJsonWithMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "created transcode job",
       |  "Status": "queued",
       |  "meta": ${ShowMetaDataHelper.defaultJson}
       |}
     """.stripMargin

  def processingJsonWithoutMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "${verboseMessage(id)}",
       |  "Status": "processing",
       |  "StatusValue": 80,
       |  "StatusUnit": "percentage"
       |}
     """.stripMargin

  def processingJsonWithMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "${verboseMessage(id)}",
       |  "Status": "processing",
       |  "StatusValue": 80,
       |  "StatusUnit": "percentage",
       |  "meta": ${ShowMetaDataHelper.defaultJson}
       |}
     """.stripMargin

  def finishedJsonWithoutMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "{verboseMessage(id)}",
       |  "Status": "finished",
       |  "DownloadSource": "${downloadSource(id)}"
       |}
     """.stripMargin

  def finishedJsonWithMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "{verboseMessage(id)}",
       |  "Status": "finished",
       |  "DownloadSource": "${downloadSource(id)}",
       |  "meta": ${ShowMetaDataHelper.defaultJson}
       |}
     """.stripMargin

  private def verboseMessage(id: Long): String = s"verboseMessage-$id"

  private def downloadSource(id: Long): Option[String] = Some(s"http://hmsservicefarm/hmsWSTranscode/api/customers/download/result-$id.mp4")

}
