package models.vimeo.video.util

import models.vimeo.video.Size

/**
  * author: cvandrei
  * since: 2016-02-02
  */
object SizeHelper {

  val pictureId = 552752804L
  val width = 1280
  val height = 720
  val link: String = SizeHelper.link(width, height, pictureId)

  def defaultSize: Size = Size(width, height, link)

  def defaultJson: String = json(width, height, pictureId)

  def json(width: Int, height: Int, pictureId: Long): String = {

    val linkLocal: String = SizeHelper.link(width, height, pictureId)
    s"""{
        |    "width": $width,
        |    "height": $height,
        |    "link": "$linkLocal"
        |}""".stripMargin

  }

  def link(width: Int, height: Int, pictureId: Long): String = s"https://i.vimeocdn.com/video/${pictureId}_${width}x$height.jpg?r=pad"

}
