import constants.VimeoEncodingStatusSystem.{VimeoEncodingStatus, IN_PROGRESS}
import org.specs2.mutable.Specification
import play.api.libs.json.Json

/**
  * Created by thiago on 1/27/16.
  */
class VimeoEncodingStatusSpec extends Specification {

  "The Json library" should {

    "parse CASE OBJECT into a JSON" in {
      val obj = Json.toJson(IN_PROGRESS)
      obj.as[VimeoEncodingStatus].name mustEqual "IN_PROGRESS"
    }

    "parse JSON into a CASE OBJECT" in {

      val json = Json.obj("name" -> "IN_PROGRESS", "$variant" -> "IN_PROGRESS")
      val obj = json.asOpt[VimeoEncodingStatus]

      obj must beSome
      obj.get.name mustEqual "IN_PROGRESS"
    }
  }
}
