package models.hms.util

import models.hms.Overlay

/**
  * author: cvandrei
  * since: 2016-02-17
  */
object OverlayHelper {

  val DEFAULT_ID = -1L
  val DEFAULT_START_OFFSET = "00:00:00,000"
  val DEFAULT_END_OFFSET = "00:00:02,000"
  val DEFAULT_LAYER = 1

  def default: Overlay = Overlay(DEFAULT_ID, Some(DEFAULT_START_OFFSET), Some(DEFAULT_END_OFFSET), DEFAULT_LAYER)

  def defaultMinimum: Overlay = Overlay(DEFAULT_ID, None, None, DEFAULT_LAYER)

  def defaultList: List[Overlay] = {

    val overlay1 = Overlay(DEFAULT_ID, Some(DEFAULT_START_OFFSET), Some(DEFAULT_END_OFFSET), DEFAULT_LAYER)
    val overlay2 = Overlay(DEFAULT_ID - 1, Some("00:00:01,000"), Some("00:00:03,300"), DEFAULT_LAYER + 1)

    List(overlay1, overlay2)

  }

  def defaultListMinimum: List[Overlay] = {

    val overlay1 = Overlay(DEFAULT_ID, None, None, DEFAULT_LAYER)
    val overlay2 = Overlay(DEFAULT_ID - 1, None, None, DEFAULT_LAYER + 1)

    List(overlay1, overlay2)

  }

  def defaultJson: String =
    s"""
       |{
       |  "ID": $DEFAULT_ID,
       |  "StartOffset": "$DEFAULT_START_OFFSET",
       |  "EndOffset": "$DEFAULT_END_OFFSET",
       |  "Layer": $DEFAULT_LAYER
       |}
     """.stripMargin

  def defaultJsonMinimum: String =
    s"""
       |{
       |  "ID": $DEFAULT_ID,
       |  "Layer": $DEFAULT_LAYER
       |}
     """.stripMargin

}
