package external.helper.model

import helper.model.ShowUtil
import models.Show
import models.vimeo.video.{Download, File}
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
      val expected = show.copy(showVideoSDUrl = file.get.linkSecure)

      // test
      val result = ShowUtil.updateSdUrl(show, file)

      // verify
      result mustEqual expected

    }

  }

  "updateHdUrl()" should {

    "with hdFile=None" in {

      // prepare
      val show = defaultShow("hdUrl", None)
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
      val expected = show.copy(showVideoSDUrl = file.get.linkSecure)

      // test
      val result = ShowUtil.updateHdUrl(show, file)

      // verify
      result mustEqual expected

    }

  }

  "atLeastSd()" should {

    "width and height below threshold" in {

      // prepare
      val download = defaultDownload(639, 359)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beFalse

    }

    "width below threshold" in {

      // prepare
      val download = defaultDownload(639, 360)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beFalse

    }

    "height below threshold" in {

      // prepare
      val download = defaultDownload(640, 359)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beFalse

    }

    "at threshold" in {

      // prepare
      val download = defaultDownload(640, 360)

      // test
      val result = ShowUtil.atLeastSd(download)

      // verify
      result must beTrue

    }

    "above threshold" in {

      // prepare
      val download = defaultDownload(641, 361)

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

    // TODO test case: file below upper bound for SD + source at upper bound for SD
    // TODO test case: file at upper bound for SD + source at upper bound for SD
    // TODO test case: file at upper bound for SD + source below upper bound for SD

    // TODO review test case
    "resolution < source" in {

      // prepare
      val file = defaultFile("sd", 640, 360, "link", "linkSecure")
      val source = defaultDownload(960, 540)

      // test
      val result = ShowUtil.sdCriteriaCheck(file, source)

      // verify
      result must beFalse

    }

    // TODO review test case
    "resolution = source" in {

      // prepare
      val file = defaultFile("sd", 960, 540, "link", "linkSecure")
      val source = defaultDownload(960, 540)

      // test
      val result = ShowUtil.sdCriteriaCheck(file, source)

      // verify
      result must beTrue

    }

    // TODO review test case
    "resolution > source" in {

      // prepare
      val file = defaultFile("sd", 960, 540, "link", "linkSecure")
      val source = defaultDownload(640, 360)

      // test
      val result = ShowUtil.sdCriteriaCheck(file, source)

      // verify
      result must beTrue

    }

    "resolution is below SD" in {

      // prepare
      val file = defaultFile("sd", 600, 300, "link", "linkSecure")
      val source = defaultDownload(960, 540)

      // test
      val result = ShowUtil.sdCriteriaCheck(file, source)

      // verify
      result must beTrue

    }

    "file undefined; source resolution at least SD" in {

      // prepare
      val source = defaultDownload(960, 540)

      // test
      val result = ShowUtil.sdCriteriaCheck(None, source)

      // verify
      result must beFalse

    }

    "file undefined; source resolution below SD" in {

      // prepare
      val source = defaultDownload(600, 300)

      // test
      val result = ShowUtil.sdCriteriaCheck(None, source)

      // verify
      result must beTrue

    }

  }

  /*
   * TEST HELPERS
   ********************************************************************************************************************/

  def defaultShow(sdUrl: String, hdUrl: Option[String]) = Show(None,
    "stationId",
    "stationName",
    "logoUrl",
    true,
    "mainColo",
    "channelId",
    "channelName",
    -1L,
    "title",
    "sourceTitle",
    "showSubtitle",
    "showLogoUrl",
    hdUrl,
    sdUrl,
    "broadcastInfo",
    "rootPortalUrl",
    None,
    None)

  def defaultFile(link: String, linkSecure: String): Some[File] = defaultFile("sd", 1280, 720, link, linkSecure)

  def defaultFile(quality: String, width: Int, height: Int, link: String, linkSecure: String): Some[File] = {

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
