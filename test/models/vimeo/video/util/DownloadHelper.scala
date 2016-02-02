package models.vimeo.video.util

import models.vimeo.video.Download

/**
  * author: cvandrei
  * since: 2016-02-02
  */
object DownloadHelper {

  val quality = "hd"
  val fileType = "video/mp4"
  val width = 1280
  val height = 720
  val expires = "2016-01-22T15:13:33+00:00"
  val link = "https://vimeo.com/api/file/download?clip_id=152690945&id=393716837&profile=113&codec=H264&exp=1453475613&sig=db6c87e0c3e2ea7706c39044beffc9f3fe666552"
  val createdTime = "2016-01-22T11:52:48+00:00"
  val fps = 25
  val size = 120692533L
  val md5 = "b2b3412e3d757943f58d661928ff81bc"

  def defaultDownload: Download = {
    Download(quality, fileType, width, height, expires, link, createdTime, fps, size, md5)
  }

  def defaultJson: String = {

    s"""{
        |    "quality": "$quality",
        |    "type": "$fileType",
        |    "width": $width,
        |    "height": $height,
        |    "expires": "$expires",
        |    "link": "$link",
        |    "created_time": "$createdTime",
        |    "fps": $fps,
        |    "size": $size,
        |    "md5": "$md5"
        |}""".stripMargin

  }

}
