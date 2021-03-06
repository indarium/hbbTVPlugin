package external.helper.model

import constants.VimeoEncodingStatusSystem.{DONE, IN_PROGRESS}
import helper.model.ShowUtil
import models.vimeo.video.{Download, File}
import models.{Show, ShowHelper}
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import play.api.test.PlayRunners

/**
  * author: cvandrei
  * since: 2016-02-09
  */
class ShowUtilSpec extends Specification with PlayRunners {

  "updateSdUrl()" should {

    "with sdFile=None" in {

      // prepare
      val show = defaultShow("sdUrl", None)
      val expected = show

      // test
      val result = ShowUtil.updateSdUrl(show, None)

      // verify
      result mustEqual expected

    }

    "with sdFile defined" in {

      // prepare
      val show = defaultShow("linkBefore", None)
      val file = defaultFile("linkAfter", "linkSecureAfter")
      val expected = show.copy(showVideoSDUrl = file.get.link)

      // test
      val result = ShowUtil.updateSdUrl(show, file)

      // verify
      result mustEqual expected

    }

  }

  "updateHdUrl()" should {

    "with hdFile=None" in {

      // prepare
      val show = defaultShow("sdUrl", None)
      val expected = show

      // test
      val result = ShowUtil.updateHdUrl(show, None)

      // verify
      result mustEqual expected

    }

    "with hdFile defined" in {

      // prepare
      val show = defaultShow("linkBefore", None)
      val file = defaultFile("linkAfter", "linkSecureAfter")
      val expected = show.copy(showVideoHDUrl = Some(file.get.link))

      // test
      val result = ShowUtil.updateHdUrl(show, file)

      // verify
      result mustEqual expected

    }

  }

  "atLeastSd()" should {

    "width and height below threshold" in {

      // prepare
      val download = defaultDownload(ShowUtil.SD_WIDTH_LOWER_BOUND - 1, ShowUtil.SD_HEIGHT_LOWER_BOUND - 1)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beFalse

    }

    "width below threshold" in {

      // prepare
      val download = defaultDownload(ShowUtil.SD_WIDTH_LOWER_BOUND - 1, ShowUtil.SD_HEIGHT_LOWER_BOUND)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beFalse

    }

    "height below threshold" in {

      // prepare
      val download = defaultDownload(ShowUtil.SD_WIDTH_LOWER_BOUND, ShowUtil.SD_HEIGHT_LOWER_BOUND - 1)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beFalse

    }

    "at threshold" in {

      // prepare
      val download = defaultDownload(ShowUtil.SD_WIDTH_LOWER_BOUND, ShowUtil.SD_WIDTH_UPPER_BOUND)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beTrue

    }

    "above threshold" in {

      // prepare
      val download = defaultDownload(ShowUtil.SD_WIDTH_LOWER_BOUND + 1, ShowUtil.SD_WIDTH_UPPER_BOUND + 1)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beTrue

    }

    "debug test with TV data" in {

      // prepare
      val download = defaultDownload(960, 528)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beTrue

    }

    "debug test with TV data" in {

      // prepare
      val download = defaultDownload(640, 352)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beTrue

    }

  }

  "atLeastHd()" should {

    "width and height below threshold" in {

      // prepare
      val download = defaultDownload(1279, 719)

      // test
      val result = ShowUtil.atLeastHd(download)

      // verify
      result must beFalse

    }

    "width below threshold" in {

      // prepare
      val download = defaultDownload(1279, 720)

      // test
      val result = ShowUtil.atLeastHd(download)

      // verify
      result must beFalse

    }

    "height below threshold" in {

      // prepare
      val download = defaultDownload(1280, 719)

      // test
      val result = ShowUtil.atLeastHd(download)

      // verify
      result must beFalse

    }

    "at threshold" in {

      // prepare
      val download = defaultDownload(1280, 720)

      // test
      val result = ShowUtil.atLeastHd(download)

      // verify
      result must beTrue

    }

    "above threshold" in {

      // prepare
      val download = defaultDownload(1281, 721)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beTrue

    }

  }

  "sdCriteriaCheck()" should {

    "file below upper bound of SD AND source at upper bound of SD resolution" in {

      // prepare
      val file = defaultFile("sd", Some(ShowUtil.SD_WIDTH_LOWER_BOUND), Some(ShowUtil.SD_HEIGHT_LOWER_BOUND), "link", "linkSecure")
      val source = defaultDownload(ShowUtil.SD_WIDTH_UPPER_BOUND, ShowUtil.SD_HEIGHT_UPPER_BOUND)

      // test
      val result = ShowUtil.sdCriteriaCheck(file, source)

      // verify
      result must beFalse

    }

    "file at upper bound of SD AND source at upper bound of SD resolution" in {

      // prepare
      val file = defaultFile("sd", Some(ShowUtil.SD_WIDTH_UPPER_BOUND), Some(ShowUtil.SD_HEIGHT_UPPER_BOUND), "link", "linkSecure")
      val source = defaultDownload(ShowUtil.SD_WIDTH_UPPER_BOUND, ShowUtil.SD_HEIGHT_UPPER_BOUND)

      // test
      val result = ShowUtil.sdCriteriaCheck(file, source)

      // verify
      result must beTrue

    }

    "file at upper bound of SD AND source below upper bound of SD resolution" in {

      // prepare
      val file = defaultFile("sd", Some(ShowUtil.SD_WIDTH_UPPER_BOUND), Some(ShowUtil.SD_HEIGHT_UPPER_BOUND), "link", "linkSecure")
      val source = defaultDownload(ShowUtil.SD_WIDTH_UPPER_BOUND, ShowUtil.SD_HEIGHT_UPPER_BOUND)

      // test
      val result = ShowUtil.sdCriteriaCheck(file, source)

      // verify
      result must beTrue

    }

    "file below SD resolution AND source has SD resolution" in {

      // prepare
      val file = defaultFile("sd", Some(500), Some(200), "link", "linkSecure")
      val source = defaultDownload(ShowUtil.SD_WIDTH_UPPER_BOUND, ShowUtil.SD_HEIGHT_UPPER_BOUND)
      ShowUtil.atLeastSd(source) must beTrue

      // test
      val result = ShowUtil.sdCriteriaCheck(file, source)

      // verify
      result must beFalse

    }

    "file at lower bound for SD; source is HD" in {

      // prepare
      val file = defaultFile("sd", Some(ShowUtil.SD_WIDTH_LOWER_BOUND), Some(ShowUtil.SD_HEIGHT_LOWER_BOUND), "link", "linkSecure")
      val source = defaultDownload(1920, 1080)

      // test
      val result = ShowUtil.sdCriteriaCheck(file, source)

      // verify
      result must beFalse

    }

    "file at upper bound for SD; source is HD" in {

      // prepare
      val file = defaultFile("sd", Some(ShowUtil.SD_WIDTH_UPPER_BOUND), Some(ShowUtil.SD_HEIGHT_UPPER_BOUND), "link", "linkSecure")
      val source = defaultDownload(1920, 1080)

      // test
      val result = ShowUtil.sdCriteriaCheck(file, source)

      // verify
      result must beTrue

    }

    "file undefined; source resolution at least SD" in {

      // prepare
      val source = defaultDownload(ShowUtil.SD_WIDTH_UPPER_BOUND, ShowUtil.SD_HEIGHT_UPPER_BOUND)

      // test
      val result = ShowUtil.sdCriteriaCheck(None, source)

      // verify
      result must beFalse

    }

    "file undefined; source resolution below SD" in {

      // prepare
      val source = defaultDownload(ShowUtil.SD_WIDTH_LOWER_BOUND - 1, ShowUtil.SD_HEIGHT_LOWER_BOUND - 1)

      // test
      val result = ShowUtil.sdCriteriaCheck(None, source)

      // verify
      result must beTrue

    }

  }

  "hdCriteriaCheck()" should {

    "file below lower HD bound; source is HD" in {

      // prepare
      val file = defaultFile("hd", Some(ShowUtil.SD_WIDTH_UPPER_BOUND), Some(ShowUtil.SD_HEIGHT_UPPER_BOUND), "link", "linkSecure")
      val source = defaultDownload(1920, 1080)

      // test
      val result = ShowUtil.hdCriteriaCheck(file, source)

      // verify
      result must beFalse

    }

    "file at upper HD bound; source at upper HD bound" in {

      // prepare
      val file = defaultFile("hd", Some(1920), Some(1080), "link", "linkSecure")
      val source = defaultDownload(1920, 1080)

      // test
      val result = ShowUtil.hdCriteriaCheck(file, source)

      // verify
      result must beTrue

    }

    "file at upper HD bound; source below upper HD bound" in {

      // prepare
      val file = defaultFile("hd", Some(1920), Some(1080), "link", "linkSecure")
      val source = defaultDownload(1280, 720)

      // test
      val result = ShowUtil.hdCriteriaCheck(file, source)

      // verify
      result must beTrue

    }

    "file undefined; source is HD" in {

      // prepare
      val source = defaultDownload(1920, 1080)

      // test
      val result = ShowUtil.hdCriteriaCheck(None, source)

      // verify
      result must beFalse

    }

    "file undefined; source below HD" in {

      // prepare
      val source = defaultDownload(ShowUtil.SD_WIDTH_UPPER_BOUND, ShowUtil.SD_HEIGHT_UPPER_BOUND)

      // test
      val result = ShowUtil.hdCriteriaCheck(None, source)

      // verify
      result must beTrue

    }

  }

  "updateEncodingStatus()" should {

    "sd=true && hd=true" in {

      // prepare
      val show = ShowHelper.withVideoUrlsAndVimeoEncodingStatus("https://sdUrl", Some("https://hdUrl"), Some(IN_PROGRESS))
      val sdFile = defaultFile("sd", Some(ShowUtil.SD_WIDTH_UPPER_BOUND), Some(ShowUtil.SD_HEIGHT_UPPER_BOUND), "http://sdUrl", "https://sdUrl")
      val hdFile = defaultFile("hd", Some(1920), Some(1080), "http://hdUrl", "https://hdUrl")
      val source = defaultDownload("source", 1920, 1080, "https://sourceUrl")

      ShowUtil.sdCriteriaCheck(sdFile, source) must beTrue
      ShowUtil.hdCriteriaCheck(hdFile, source) must beTrue

      val expected = show.copy(vimeoEncodingStatus = Some(DONE))

      // test
      val result = ShowUtil.updateEncodingStatus(show, sdFile, hdFile, source)

      // verify
      result mustEqual expected

    }

    "sd=true && hd=false" in {

      // prepare
      val show = ShowHelper.withVideoUrlsAndVimeoEncodingStatus("https://sdUrl", Some("https://hdUrl"), Some(IN_PROGRESS))
      val sdFile = defaultFile("sd", Some(ShowUtil.SD_WIDTH_UPPER_BOUND), Some(ShowUtil.SD_HEIGHT_UPPER_BOUND), "http://sdUrl", "https://sdUrl")
      val hdFile = defaultFile("hd", Some(1280), Some(720), "http://hdUrl", "https://hdUrl")
      val source = defaultDownload("source", 1920, 1080, "https://sourceUrl")

      ShowUtil.sdCriteriaCheck(sdFile, source) must beTrue
      ShowUtil.hdCriteriaCheck(hdFile, source) must beFalse

      val expected = show

      // test
      val result = ShowUtil.updateEncodingStatus(show, sdFile, hdFile, source)

      // verify
      result mustEqual expected

    }

    "sd=false && hd=true" in {

      // prepare
      val show = ShowHelper.withVideoUrlsAndVimeoEncodingStatus("https://sdUrl", Some("https://hdUrl"), Some(IN_PROGRESS))
      val sdFile = defaultFile("sd", Some(ShowUtil.SD_WIDTH_LOWER_BOUND), Some(ShowUtil.SD_HEIGHT_LOWER_BOUND - 1), "http://sdUrl", "https://sdUrl")
      val hdFile = defaultFile("hd", Some(1920), Some(1080), "http://hdUrl", "https://hdUrl")
      val source = defaultDownload("source", 1920, 1080, "https://sourceUrl")

      ShowUtil.sdCriteriaCheck(sdFile, source) must beFalse
      ShowUtil.hdCriteriaCheck(hdFile, source) must beTrue

      val expected = show

      // test
      val result = ShowUtil.updateEncodingStatus(show, sdFile, hdFile, source)

      // verify
      result mustEqual expected

    }

    "sd=true && hd=false" in {

      // prepare
      val show = ShowHelper.withVideoUrlsAndVimeoEncodingStatus("https://sdUrl", Some("https://hdUrl"), Some(IN_PROGRESS))
      val sdFile = defaultFile("sd", Some(ShowUtil.SD_WIDTH_LOWER_BOUND), Some(360), "http://sdUrl", "https://sdUrl")
      val hdFile = defaultFile("hd", Some(1280), Some(720), "http://hdUrl", "https://hdUrl")
      val source = defaultDownload("source", 1920, 1080, "https://sourceUrl")

      ShowUtil.sdCriteriaCheck(sdFile, source) must beFalse
      ShowUtil.hdCriteriaCheck(hdFile, source) must beFalse

      val expected = show

      // test
      val result = ShowUtil.updateEncodingStatus(show, sdFile, hdFile, source)

      // verify
      result mustEqual expected

    }

    "debug test with TV data: source=720x576, sd=960x528" in {

      // prepare
      val show = ShowHelper.withVideoUrlsAndVimeoEncodingStatus("https://sdUrl", Some("https://hdUrl"), Some(IN_PROGRESS))
      val sdFile = defaultFile("sd", Some(960), Some(528), "http://sdUrl", "https://sdUrl")
      val hdFile = None
      val source = defaultDownload("source", 720, 576, "https://sourceUrl")

      ShowUtil.sdCriteriaCheck(sdFile, source) must beTrue
      ShowUtil.hdCriteriaCheck(hdFile, source) must beTrue

      val expected = show.copy(vimeoEncodingStatus = Some(DONE))

      // test
      val result = ShowUtil.updateEncodingStatus(show, sdFile, hdFile, source)

      // verify
      result mustEqual expected

    }

    "debug test with TV data: source=720x576, sd=640x352" in {

      // prepare
      val show = ShowHelper.withVideoUrlsAndVimeoEncodingStatus("https://sdUrl", Some("https://hdUrl"), Some(IN_PROGRESS))
      val sdFile = defaultFile("sd", Some(640), Some(352), "http://sdUrl", "https://sdUrl")
      val hdFile = None
      val source = defaultDownload("source", 720, 576, "https://sourceUrl")

      ShowUtil.sdCriteriaCheck(sdFile, source) must beFalse
      ShowUtil.hdCriteriaCheck(hdFile, source) must beTrue

      val expected = show

      // test
      val result = ShowUtil.updateEncodingStatus(show, sdFile, hdFile, source)

      // verify
      result mustEqual expected

    }

  }

  /*
   * TEST HELPERS
   ********************************************************************************************************************/

  def defaultShow(sdUrl: String, hdUrl: Option[String]): Show = ShowHelper.withVideoUrlsAndVimeoEncodingStatus(sdUrl, hdUrl, None)

  def defaultFile(link: String, linkSecure: String): Some[File] = defaultFile("sd", Some(1280), Some(720), link, linkSecure)

  def defaultFile(quality: String, width: Option[Int], height: Option[Int], link: String, linkSecure: String): Some[File] = {

    Some(
      File(quality, "video/mp4", width, height, link, linkSecure, DateTime.now.toString, 25, 1000000L, "md5")
    )
  }

  def defaultDownload(width: Int, height: Int): Download = defaultDownload("source", width, height, "link")

  def defaultDownload(width: Int, height: Int, link: String): Download = defaultDownload("source", width, height, link)

  def defaultDownload(quality: String, width: Int, height: Int, link: String): Download = {

    Download(
      quality,
      "video/mp4",
      width,
      height,
      DateTime.now.plusDays(1).toString,
      link,
      DateTime.now.plusMinutes(-1).toString,
      25,
      1000000L,
      "md5")

  }

}
