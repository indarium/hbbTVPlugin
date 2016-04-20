package helper

import java.net.MalformedURLException

import models.ShowHelper
import org.specs2.mutable.Specification
import play.api.test.{PlayRunners, FakeApplication}

/**
  * author: cvandrei
  * since: 2016-04-20
  */
class S3UtilSpec extends Specification with PlayRunners {

  "S3Util" should {

    "extractS3FileName() with cdnUri" in {
      running(FakeApplication()) {

        // prepare
        val fileName = "foo/bla/123.mp4"
        val sdUrl = "%s/%s".format(Config.cdnBaseUrl, fileName)
        val show = ShowHelper.defaultMinimumObject("SAT", "MV1", -111L, sdUrl)

        // test
        val actual = S3Util.extractS3FileName(show)

        // verify
        actual mustEqual fileName

      }
    }

    "extractS3FileName() without cdnUri" in {
      running(FakeApplication()) {

        // prepare
        val fileName = "foo/bla/123.mp4"
        val baseUrl = "https://indarium.de:8080"
        baseUrl mustNotEqual Config.cdnBaseUrl
        val sdUrl = "%s/%s".format(baseUrl, fileName)
        val show = ShowHelper.defaultMinimumObject("SAT", "MV1", -111L, sdUrl)

        // test
        val actual = S3Util.extractS3FileName(show)

        // verify
        actual mustEqual fileName

      }
    }

    "extractS3FileName() invalid uri" in {
      running(FakeApplication()) {

        // prepare
        val sdUrl = "indarium.de:8080/foo/bla/123.mp4"
        val show = ShowHelper.defaultMinimumObject("SAT", "MV1", -111L, sdUrl)

        // test
        S3Util.extractS3FileName(show) must throwA[MalformedURLException]

      }
    }

  }

}
