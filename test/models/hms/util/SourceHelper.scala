package models.hms.util

import models.hms.Source
import play.api.libs.json.Json

/**
  * author: cvandrei
  * since: 2016-02-17
  */
object SourceHelper {

  val DEFAULT_ID = -1L
  val DEFAULT_SOURCE_NAME = Some(s"sourceName-$DEFAULT_ID")
  val DEFAULT_START_OFFSET = Some("00:00:10,400")
  val DEFAULT_END_OFFSET = Some("00:30:00,880")
  val DEFAULT_OVERLAYS = Some(OverlayHelper.defaultList)
  val DEFAULT_PROFILE = "D1_PAL_WEB_HIGH"

  def default(id: Long, profile: String): Source = Source(id,
      Some(s"sourceName-$id"),
      DEFAULT_START_OFFSET,
      DEFAULT_END_OFFSET,
      s"destinationName-$id",
      DEFAULT_OVERLAYS,
      profile
    )

  def defaultMinimum: Source = Source(DEFAULT_ID,
      None,
      None,
      None,
      s"destinationName-$DEFAULT_ID",
      None,
      DEFAULT_PROFILE
    )

  def defaultList: List[Source] = {

    val source1 = default(DEFAULT_ID, "D1_PAL_WEB_HIGH")
    val source2 = default(DEFAULT_ID - 1, "HDTV_WEB_HIGH")

    List(source1, source2)

  }

  def defaultJson: String = {

    val overlays = Json.toJson(OverlayHelper.defaultList).toString()

    s"""{
        |  "ID": $DEFAULT_ID,
        |  "SourceName": "sourceName-$DEFAULT_ID",
        |  "StartOffset": "$DEFAULT_START_OFFSET",
        |  "EndOffset": "$DEFAULT_END_OFFSET",
        |  "DestinationName": "destionationName-$DEFAULT_ID",
        |  "Overlays": $overlays,
        |  "Profile": "$DEFAULT_PROFILE"
        |}
     """.stripMargin

  }

  def defaultJsonMinimum: String =
    s"""{
        |  "ID": $DEFAULT_ID,
        |  "DestinationName": "destionationName-$DEFAULT_ID",
        |  "Profile": "$DEFAULT_PROFILE"
        |}
     """.stripMargin

}
