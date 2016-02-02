package models.vimeo.video

import models.vimeo.video.util.SizeHelper

/**
  * author: cvandrei
  * since: 2016-02-02
  */
object PicturesHelper {

  val vimeoId = 152690945L
  val pictureId = 552752804L
  val uri = s"/videos/$vimeoId/pictures/$pictureId"
  val active = true
  val picturesType = "custom"
  val width1 = 100
  val height1 = 75
  val link1 = SizeHelper.link(width1, height1, pictureId)
  val width2 = 1280
  val height2 = 720
  val link2 = SizeHelper.link(width2, height2, pictureId)

  def defaultPictures: Pictures = {
    Pictures(uri, active, picturesType, Seq())
  }

  def defaultJson: String = {

    s"""{
        |"pictures": {
        |    "uri": "$uri",
        |    "active": $active,
        |    "type": "$picturesType",
        |    "sizes": [
        |    ${SizeHelper.json(width1, height1, pictureId)},
        |    ${SizeHelper.json(width2, height2, pictureId)}
        |    ]
        |}
        |}""".stripMargin

  }

}
