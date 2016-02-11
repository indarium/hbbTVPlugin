package models.vimeo.video.util

import models.vimeo.video.File

/**
  * author: cvandrei
  * since: 2016-02-02
  */
object FileHelper {

  val quality = "hd"
  val fileType = "video/mp4"
  val width = Some(1280)
  val height = Some(720)
  val link = "http://player.vimeo.com/external/152690945.hd.mp4?s=e514c83b1988801c9067e150d2470e32bfc1c2c0&profile_id=113&oauth2_token_id=393716837"
  val linkSecure = "https://player.vimeo.com/external/152690945.hd.mp4?s=e514c83b1988801c9067e150d2470e32bfc1c2c0&profile_id=113&oauth2_token_id=393716837"
  val createdTime = "2016-01-22T11:52:48+00:00"
  val fps = 25
  val size = 120692533L
  val md5 = "b2b3412e3d757943f58d661928ff81bc"

  def defaultFile: File = {
    File(quality, fileType, width, height, link, linkSecure, createdTime, fps, size, md5)
  }

  def defaultJson: String = {

    val widthValue: String = if (width.isEmpty) "" else s"""|    "width": ${width.get},"""
    val heightValue: String = if (height.isEmpty) "" else  s"""|    "height": ${height.get},"""
    s"""{
        |    "quality": "$quality",
        |    "type": "$fileType",
        $widthValue
        $heightValue
        |    "link": "$link",
        |    "link_secure": "$linkSecure",
        |    "created_time": "$createdTime",
        |    "fps": $fps,
        |    "size": $size,
        |    "md5": "$md5"
        |}""".stripMargin

  }

}
