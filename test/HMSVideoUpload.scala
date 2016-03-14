
import java.io.File

import com.ning.http.client.AsyncHttpClientConfig
import helper.hms.HMSApi
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.{JsObject, JsArray, Json}
import play.api.libs.ws.WS

import scala.concurrent.Await
import scala.concurrent.duration._

import org.specs2.mutable._
import org.specs2.matcher._

import play.api.{Logger, Play}
import play.api.Play.current
import play.api.test._
import play.api.test.Helpers._

/**
 * Created by dermicha on 16/06/15.
 **/

case class MediaObjectId(ID: Long)

object MediaObjectId {
  implicit val mediaObjectIdFormat = Json.format[MediaObjectId]
}

case class MediaObjectResult(Sources: Seq[MediaObjectId])

object MediaObjectResult {
  implicit val mediaObjectResultFormat = Json.format[MediaObjectResult]
}

class HMSVideoUpload extends PlaySpec with OneAppPerSuite with ScalaFutures {

  implicit override lazy val app: FakeApplication = FakeApplication(withGlobal = Some(GlobalTestSettings))

  "HMSVideoUpload" should {

    "upload a video" in {
      val baseUrl = "https://62.67.13.54"
      val authHeader = "Access-Token"
      val fullPathCreate = s"$baseUrl/hmsWSMedia/api/media/SAT"
      val file1 = new File("/Users/dermicha/tmp/MediaCoder_test1_1m9s_AVC_VBR_256kbps_640x480_24fps_MPEG2Layer3_CBR_160kbps_Stereo_22050Hz.mp4")
      val file2 = new File("/Users/dermicha/tmp/H264_test1_Talkinghead_mp4_480x360.mp4")
      val file3 = new File("/Users/dermicha/tmp/spinning-logo.mp4")

      val file = file3

      if (file.canRead)
        Logger.debug(s"canRead: ${file.getCanonicalPath}")

      val accessToken = Await.result(HMSApi.authenticate, Duration(5, SECONDS)).get

      Logger.debug(s"accessToken: $accessToken")


      val mediaObject = Json.obj(
        "Sources" -> Json.arr(
          Json.obj("DisplayName" -> s"Testsendung ${java.util.UUID.randomUUID().toString}",
            "FileName" -> file.getName,
            "MediaType" -> "MPEG4",
            "ExpiryDate" -> "2022-12-31T23:59:59,999Z",
            "FileSize" -> file.length(),
            "ThirdPartyID" -> java.util.UUID.randomUUID().toString
          )
        ),
        "PushErrorNotification" -> "false"
      )

      //"NotificationCallback" -> ""

      Logger.info(s"new media object: ${Json.prettyPrint(mediaObject)}")
      Logger.info(s"fullPathCreate: $fullPathCreate")
      val timePoint1 = System.currentTimeMillis()

      val newMediaResult = Await.result(
        WS.url(fullPathCreate)
          .withHeaders("x-api-version" -> "1.0")
          .withHeaders("Content-Type" -> "application/json")
          .withHeaders(authHeader -> accessToken.Access_Token)
          .withBody(mediaObject)
          .withMethod("POST")
          .post[JsObject](mediaObject)
        , Duration(20, SECONDS))

      val timePoint2 = System.currentTimeMillis()
      Logger.info(s"MediaObject creation took: ${(timePoint2 - timePoint1) / 1000}s")
      Logger.info(s"newMediaResult: ${newMediaResult.body}")

      newMediaResult.status mustBe 200

      val mediaObjectResult = Json.parse(newMediaResult.body).as[MediaObjectResult]
      val assetId = mediaObjectResult.Sources(0).ID

      val fullPathUpload = s"$baseUrl/hmsWSMedia/api/Upload/SAT?ID=$assetId"
      Logger.info(s"fullPathUpload: $fullPathUpload")

      import com.ning.http.multipart.FilePart
      import com.ning.http.client.RequestBuilder
      import com.ning.http.client.AsyncHttpClient

      val filePart = new FilePart(file.getName, file)
      val builder = new RequestBuilder()
        .setUrl(fullPathUpload)
        .addBodyPart(filePart)
        .addHeader(authHeader, accessToken.Access_Token)
        .addHeader("X-API-VERSION", "1.0")
        .addHeader("X-CONTENT-FILE-NAME", file.getName)
        .setMethod("POST")

      var config: AsyncHttpClientConfig.Builder = new AsyncHttpClientConfig.Builder
      config.setRequestTimeoutInMs(-1)
      config.setIdleConnectionTimeoutInMs(-1)

      val timePoint3 = System.currentTimeMillis()
      val client = new AsyncHttpClient(config.build()).prepareRequest(builder.build())
      val response = client.execute().get(5, MINUTES)
      val timePoint4 = System.currentTimeMillis()
      Logger.info(s"MediaObject upload took: ${(timePoint4 - timePoint3) / 1000}s")

      Logger.info(s"response: ${response.getResponseBody}")

      response.getStatusCode mustBe 200
    }
  }
}