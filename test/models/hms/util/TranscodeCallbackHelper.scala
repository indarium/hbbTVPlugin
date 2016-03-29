package models.hms.util

import constants.HmsCallbackStatus
import models.dto.util.ShowMetaDataHelper
import models.hms.TranscodeCallback

/**
  * author: cvandrei
  * since: 2016-02-19
  */
object TranscodeCallbackHelper {

  val DEFAULT_META = Some(ShowMetaDataHelper.defaultObject("MV1", "SAT", -1001L))

  def queuedObjectWithoutMeta(id: Long): TranscodeCallback = TranscodeCallback(id, Some("created transcode job"), HmsCallbackStatus.QUEUED, None, None, None, None)

  def queuedObjectWithMeta(id: Long): TranscodeCallback = TranscodeCallback(id, Some("created transcode job"), HmsCallbackStatus.QUEUED, None, None, None, DEFAULT_META)

  def processingObjectWithoutMeta(id: Long): TranscodeCallback = TranscodeCallback(id, verboseMessage(id), HmsCallbackStatus.PROCESSING, Some(80), Some("percentage"), None, None)

  def processingObjectWithMeta(id: Long): TranscodeCallback = TranscodeCallback(id, verboseMessage(id), HmsCallbackStatus.PROCESSING, Some(80), Some("percentage"), None, DEFAULT_META)

  def finishedObjectWithoutMeta(id: Long): TranscodeCallback = TranscodeCallback(id, verboseMessage(id), HmsCallbackStatus.FINISHED, None, None, downloadSource(id), None)

  def finishedObjectWithMeta(id: Long): TranscodeCallback = TranscodeCallback(id, verboseMessage(id), HmsCallbackStatus.FINISHED, None, None, downloadSource(id), DEFAULT_META)

  def queuedJsonWithoutMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "created transcode job",
       |  "Status": "${HmsCallbackStatus.QUEUED}",
       |  "created": "2016-02-17T09:54:22.440Z",
       |  "modified": "2016-02-17T10:54:22.440Z"
       |}
     """.stripMargin

  def queuedJsonWithMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "created transcode job",
       |  "Status": "${HmsCallbackStatus.QUEUED}",
       |  "meta": ${ShowMetaDataHelper.defaultJson},
       |  "created": "2016-02-17T09:54:22.440Z",
       |  "modified": "2016-02-17T10:54:22.440Z"
       |}
     """.stripMargin

  def processingJsonWithoutMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "${verboseMessage(id)}",
       |  "Status": "${HmsCallbackStatus.PROCESSING}",
       |  "StatusValue": 80,
       |  "StatusUnit": "percentage",
       |  "created": "2016-02-17T09:54:22.440Z",
       |  "modified": "2016-02-17T10:54:22.440Z"
       |}
     """.stripMargin

  def processingJsonWithMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "${verboseMessage(id)}",
       |  "Status": "${HmsCallbackStatus.PROCESSING}",
       |  "StatusValue": 80,
       |  "StatusUnit": "percentage",
       |  "meta": ${ShowMetaDataHelper.defaultJson},
       |  "created": "2016-02-17T09:54:22.440Z",
       |  "modified": "2016-02-17T10:54:22.440Z"
       |}
     """.stripMargin

  def finishedJsonWithoutMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "{verboseMessage(id)}",
       |  "Status": "${HmsCallbackStatus.FINISHED}",
       |  "DownloadSource": "${downloadSource(id)}",
       |  "created": "2016-02-17T09:54:22.440Z",
       |  "modified": "2016-02-17T10:54:22.440Z"
       |}
     """.stripMargin

  def finishedJsonWithMeta(id: Long): String =
    s"""
       |{
       |  "ID":  $id,
       |  "VerboseMessage": "{verboseMessage(id)}",
       |  "Status": "${HmsCallbackStatus.FINISHED}",
       |  "DownloadSource": "${downloadSource(id)}",
       |  "meta": ${ShowMetaDataHelper.defaultJson},
       |  "created": "2016-02-17T09:54:22.440Z",
       |  "modified": "2016-02-17T10:54:22.440Z"
       |}
     """.stripMargin

  private def verboseMessage(id: Long): Option[String] = Some(s"verboseMessage-$id")

  private def downloadSource(id: Long): Option[String] = Some(s"http://hmsservicefarm/hmsWSTranscode/api/customers/download/result-$id.mp4")

}
