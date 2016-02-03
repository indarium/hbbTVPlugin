package external.webjazz.util

import models.rest.{Thumbnail, WebjazzNotification}
import models.vimeo.video.Size
import play.api.libs.json.{JsValue, Json}

/**
  * author: cvandrei
  * since: 2016-02-03
  */
object WebjazzUtil {

  def sizeToThumbnail(size: Size): Thumbnail = Thumbnail(size.width, size.height, size.link)

  def sizeListToThumbnails(sizes: List[Size]): List[Thumbnail] = {

    var i = 0

    List.fill(sizes.size) {
      val size = sizes(i)
      i += 1
      sizeToThumbnail(size)
    }

  }

  def createWebjazzNotification(webjazzToken: String,
                                vimeoId: Long,
                                hmsId: Long,
                                width: Int,
                                height: Int,
                                sizeList: List[Size]
                               ): JsValue = {

    val thumbnails = WebjazzUtil.sizeListToThumbnails(sizeList)
    val webjazzNotification = WebjazzNotification(webjazzToken, vimeoId, hmsId, width, height, thumbnails)

    Json.toJson(webjazzNotification)

  }

}
