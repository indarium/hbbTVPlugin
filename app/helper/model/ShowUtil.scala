package helper.model

import constants.VimeoEncodingStatusSystem.DONE
import models.Show
import models.vimeo.video.{Download, File}

/**
  * author: cvandrei
  * since: 2016-02-09
  */
object ShowUtil {

  /**
    * Set the sd url on the given show.
    *
    * @param show   returned object is a copy of this object
    * @param sdFileIn source for the sd url
    * @return an updated copy of the given show
    */
  def updateSdUrl(show: Show, sdFileIn: Option[File]): Show = {

    sdFileIn match {
      case Some(sdFile) => show.copy(showVideoSDUrl = sdFile.linkSecure)
      case None => show
    }

  }

  /**
    * Set the hd url on the given show.
    *
    * @param show   returned object is a copy of this object
    * @param hdFileIn source for the hd url
    * @return an updated copy of the given show
    */
  def updateHdUrl(show: Show, hdFileIn: Option[File]): Show = {

    hdFileIn match {
      case Some(hdFile) => show.copy(showVideoSDUrl = hdFile.linkSecure)
      case None => show
    }

  }

  /**
    * Tells us if the given source file's resolution is at least an SD video.
    *
    * @param source source file
    * @return true if resolution is at least SD
    */
  def atLeastSd(source: Download): Boolean = source.width >= 640 && source.height >= 360

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
    * @param source source file
    * @return true if Vimeo should be done encoding all SD videos we'd want
    */
  def sdCriteriaCheck(sdFileIn: Option[File], source: Download): Boolean = {

    val sourceAtLeastSd = atLeastSd(source)

    sdFileIn match {

      case Some(sdFile) =>

        sourceAtLeastSd match {

          case true =>

            atLeastHd(source) match {
              case true => sdFile.width == 960 && sdFile.height == 540
              case false => sdFile.width >= source.width && sdFile.height >= source.height
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
        source.width <= hdFile.width && source.height <= hdFile.height

      case false => true
      case _ => false

    }

  }

  def updateEncodingStatus(show: Show, sdFile: Option[File], hdFile: Option[File], source: Download): Show = {

    val sdCriteriaFulfilled = sdCriteriaCheck(sdFile, source)
    val hdCriteriaFulfilled = hdCriteriaCheck(hdFile, source)

    sdCriteriaFulfilled && hdCriteriaFulfilled match {
      case true => show.copy(vimeoEncodingStatus = Some(DONE))
      case false => show
    }

  }

}
