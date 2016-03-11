package helper

import org.specs2.mutable.Specification
import play.api.test.{FakeApplication, PlayRunners}

/**
  * These tests are meant to be debug tests (tests we use to debug functionality).
  * Feel free to convert them to proper tests that runs all possible test cases against the appropriate configuration.
  *
  * Created by cvandrei on 2016-03-11.
  */
class ConfigSpec extends Specification with PlayRunners {

  "vimeo activation config" should {

    "vimeo.activate.global" in {
      running(FakeApplication()) {

        // test
        val vimeoActivateGlobal = Config.vimeoActivateGlobal

        // verify
        vimeoActivateGlobal mustEqual false

      }
    }

    "vimeo.activate.channels" in {
      running(FakeApplication()) {

        // test
        val vimeoActivateChannel = Config.vimeoActivateChannels

        // verify
        val expected = Array("mv1", "wis")
        vimeoActivateChannel mustEqual expected

      }
    }

    "vimeo.deactivate.channels" in {
      running(FakeApplication()) {

        // test
        val vimeoDeactivateChannel = Config.vimeoDeactivateChannels

        // verify
        vimeoDeactivateChannel mustEqual Array.empty

      }
    }

  }

}
