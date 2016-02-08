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

  /**
    * Gives us the https url for the SD video with the highest resolution available.
    *
    * @param files can be empty
    * @return None if no sd file is found or it's https url is missing
    */
  def sdUrl(files: List[File]): Option[String] = highestResolutionUrl("sd", files)

  /**
    * Gives us the https url for the HD video with the highest resolution available.
    *
    * @param files can be empty
    * @return None if no hd file is found or it's https url is missing
    */
  def hdUrl(files: List[File]): Option[String] = highestResolutionUrl("hd", files)

  private def highestResolutionUrl(quality: String, files: List[File]): Option[String] = {

    var width = -1
    var linkSecure = ""

    for (file <- files) {

      if (file.quality == quality && width < file.width) {
        width = file.width
        linkSecure = file.linkSecure
      }

    }

    linkSecure match {
      case s if s.length > 0 => Some(linkSecure)
      case _ => None
    }

  }

  def downloadSource(downloads: List[Download]): Option[Download] = {

    // TODO unit tests
    var result: Option[Download] = None

    for (download <- downloads) {
      if (download.quality == "source") {
        result = Some(download)
      }
    }

    result

  }

}
