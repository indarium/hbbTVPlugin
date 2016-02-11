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
    * Gives us the file object with quality SD and the highest resolution available.
    *
    * @param files can be empty
    * @return None if no sd file is found or it's https url is missing
    */
  def sdFile(files: List[File]): Option[File] = highestResolutionUrl("sd", files)

  /**
    * Gives us the file object with quality HD and the highest resolution available.
    *
    * @param files can be empty
    * @return None if no hd file is found or it's https url is missing
    */
  def hdFile(files: List[File]): Option[File] = highestResolutionUrl("hd", files)

  private def highestResolutionUrl(quality: String, files: List[File]): Option[File] = {

    var result: Option[File] = None

    for (file <- files) {

      if (file.quality == quality && (result.isEmpty || result.get.width.get < file.width.get)) {
        result = Some(file)
      }

    }

    result

  }

  /**
    * Gives us the download with quality="source" from a given list of downloads.
    *
    * @param downloads can be empty
    * @return None if no element with quality="source" is found
    */
  def downloadSource(downloads: List[Download]): Option[Download] = {

    var result: Option[Download] = None

    for (download <- downloads) {
      if (download.quality == "source") {
        result = Some(download)
      }
    }

    result

  }

}
