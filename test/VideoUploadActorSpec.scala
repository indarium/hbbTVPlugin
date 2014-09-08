import java.io.{File, FileOutputStream}

import actors.VideoUploadActor
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.amazonaws.auth.BasicAWSCredentials
import helper.{VideoUploadFailure, S3Backend, ShowMetaData, VideoUploadSuccess}
import org.specs2.matcher.ThrownMessages
import org.specs2.mutable.SpecificationLike
import play.api.Play
import play.api.test.FakeApplication
import play.api.test.Helpers._

/**
 * Test the video upload actor. Requires credentials to be set.
 *
 * @author Matthias L. Jugel
 */
class VideoUploadActorSpec extends TestKit(ActorSystem("test")) with SpecificationLike with ImplicitSender with ThrownMessages {
  sequential

  private val BUCKET = "ac846539-6757-4284-a4d7-ce227d87a7ab"

  private val CONTENT = "THIS IS AN EXAMPLE FILE"
  private val FILE = File.createTempFile("upload.", ".tmp")
  FILE.deleteOnExit()

  private val os = new FileOutputStream(FILE)
  os.write(CONTENT.getBytes("UTF-8"))
  os.flush()
  os.close()

  private val LOCAL_FILE_URL = FILE.toURI.toURL

  private val STATION = "SPACE"
  private val CHANNEL = "CHANNEL-5"

  "VideoUploadActor" should {
    "be able to upload a file" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)

        val videoUploadActor = TestActorRef(new VideoUploadActor(new S3Backend(credentials, BUCKET)))

        val meta = new ShowMetaData(STATION, CHANNEL)
        meta.sourceVideoUrl = Some(LOCAL_FILE_URL)
        meta.localVideoFile = Some(FILE)

        videoUploadActor ! meta

        expectMsgType[VideoUploadSuccess].meta.publicVideoUrl.isDefined must beTrue
      }
    }

    "reply with a failure in case of an error" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)

        val videoUploadActor = TestActorRef(new VideoUploadActor(new S3Backend(credentials, BUCKET)))

        val meta = new ShowMetaData(STATION, CHANNEL)
        meta.sourceVideoUrl = Some(LOCAL_FILE_URL)
        meta.localVideoFile = Some(new File("/this/file/does/not/exist"))

        videoUploadActor ! meta

        expectMsgType[VideoUploadFailure].meta.publicVideoUrl.isDefined must beFalse
      }

    }

    "reply with a failure in case of a missing file" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)

        val videoUploadActor = TestActorRef(new VideoUploadActor(new S3Backend(credentials, BUCKET)))

        val meta = new ShowMetaData(STATION, CHANNEL)
        meta.sourceVideoUrl = Some(LOCAL_FILE_URL)

        videoUploadActor ! meta

        expectMsgType[VideoUploadFailure].meta.publicVideoUrl.isDefined must beFalse
      }

    }
  }
}
