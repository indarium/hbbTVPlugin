package external.vimeo

import models.vimeo.video.{Download, File, Pictures}
import play.api.libs.json.JsValue

/**
  * author: cvandrei
  * since: 2016-02-03
  */
object VideoStatusUtil {

  def extractWidth(json: JsValue): Int = (json \ "width").validate[Int].get

  def extractHeight(json: JsValue): Int = (json \ "height").validate[Int].get

  def extractPictures(json: JsValue): Pictures = (json \ "pictures").validate[Pictures].get

  def extractDownloads(json: JsValue): List[Download] = (json \ "download").validate[List[Download]].get

  def extractFiles(json: JsValue): List[File] = (json \ "files").validate[List[File]].get

}
