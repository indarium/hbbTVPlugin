package helper.model

import constants.VimeoEncodingStatusSystem.DONE
import models.Show
import models.vimeo.video.{Download, File}

/**
  * author: cvandrei
  * since: 2016-02-09
  */
object ShowUtil {

  val SD_WIDTH_LOWER_BOUND = 640
  val SD_HEIGHT_LOWER_BOUND = 300

  val SD_WIDTH_UPPER_BOUND = 960
  val SD_HEIGHT_UPPER_BOUND = 540

  /**
    * Set the sd url on the given show.
    *
    * @param show     returned object is a copy of this object
    * @param sdFileIn source for the sd url
    * @return an updated copy of the given show
    */
  def updateSdUrl(show: Show, sdFileIn: Option[File]): Show = {

    sdFileIn match {
      case Some(sdFile) => show.copy(showVideoSDUrl = sdFile.link)
      case None => show
    }

  }

  /**
    * Set the hd url on the given show.
    *
    * @param show     returned object is a copy of this object
    * @param hdFileIn source for the hd url
    * @return an updated copy of the given show
    */
  def updateHdUrl(show: Show, hdFileIn: Option[File]): Show = {

    hdFileIn match {
      case Some(hdFile) => show.copy(showVideoHDUrl = Some(hdFile.link))
      case None => show
    }

  }

  /**
    * Tells us if the given source file's resolution is at least an SD video.
    *
    * @param source source file
    * @return true if resolution is at least SD
    */
  def atLeastSd(source: Download): Boolean = source.width >= SD_WIDTH_LOWER_BOUND && source.height >= SD_HEIGHT_LOWER_BOUND

  /**
    * Tells us if the given source file's resolution is at least an HD video.
    *
    * @param source source file
    * @return true if resolution is at least HD
    */
  def atLeastHd(source: Download): Boolean = source.width >= 1280 && source.height >= 720

  /**
    * Tells us if we already have an SD video with the expected resolution.
    *
    * @param sdFileIn current sdFile
    * @param source   source file
    * @return true if Vimeo should be done encoding all SD videos we'd want
    */
  def sdCriteriaCheck(sdFileIn: Option[File], source: Download): Boolean = {

    val sourceAtLeastSd = atLeastSd(source)

    sdFileIn match {

      case Some(sdFile) =>

        sourceAtLeastSd match {

          case true =>

            atLeastHd(source) match {
              case true => sdFile.width == Some(SD_WIDTH_UPPER_BOUND)
              case false => sdFile.width.get >= source.width
            }

          case false => false
        }

      case None =>

        sourceAtLeastSd match {
          case true => false
          case false => true
        }

    }

  }

  /**
    * Tells us if we already have an HD video with the expected resolution.
    *
    * @param hdFileIn current hdFile
    * @param source   source file
    * @return true if Vimeo should be done encoding all HD videos we'd want
    */
  def hdCriteriaCheck(hdFileIn: Option[File], source: Download): Boolean = {

    val sourceIsHd = atLeastHd(source)

    sourceIsHd match {

      case true if hdFileIn.isDefined =>

        val hdFile = hdFileIn.get
        source.width <= hdFile.width.get && source.height <= hdFile.height.get

      case false => true
      case _ => false

    }

  }

  /**
    * Changes vimeoEncodingStatus when no further video status calls to Vimeo are necessary.
    */
  def updateEncodingStatus(show: Show, sdFile: Option[File], hdFile: Option[File], source: Download): Show = {

    val sdCriteriaFulfilled = sdCriteriaCheck(sdFile, source)
    val hdCriteriaFulfilled = hdCriteriaCheck(hdFile, source)

    sdCriteriaFulfilled && hdCriteriaFulfilled match {
      case true => show.copy(vimeoEncodingStatus = Some(DONE))
      case false => show
    }

  }

}
