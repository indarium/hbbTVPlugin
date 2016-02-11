import java.io.{File, FileOutputStream}

import actors.ShowProcessingActor
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.amazonaws.auth.BasicAWSCredentials
import helper.{Config, S3Backend, ShowMetaData}
import org.specs2.matcher.ThrownMessages
import org.specs2.mutable.SpecificationLike
import play.api.test.FakeApplication
import play.api.test.Helpers._

/**
 * Test the show processing actor.
 *
 * @author Matthias L. Jugel
 */
class ShowProcessingActorSpec extends TestKit(ActorSystem("test")) with SpecificationLike with ImplicitSender with ThrownMessages{
  sequential

  private val BUCKET = "ac846539-6757-4284-a4d7-ce227d87a7ab"

  private val CONTENT = "THIS IS AN EXAMPLE FILE"
  private val FILE = File.createTempFile("upload.", ".tmp")
  FILE.deleteOnExit()

  val os = new FileOutputStream(FILE)
  os.write(CONTENT.getBytes("UTF-8"))
  os.flush()
  os.close()

  private val STATION = "SPACE"
  private val CHANNEL = "CHANNEL-5"
  private val TITLE = "Ulala"

  "ShowProcessingActor" should {
    "download a a video file, upload it and provide a public url" in {
      running(FakeApplication()) {

        val awsAccessKeyId: String = Config.awsAccessKeyId
        val awsSecretKey: String = Config.awsSecretKey

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)

        val s3Backend: S3Backend = new S3Backend(credentials, BUCKET)
        val showProcessingActor = TestActorRef(new ShowProcessingActor(s3Backend))

        s3Backend.delete("%s/%s/%s".format(STATION, CHANNEL, TITLE))

        s3Backend.list().length mustEqual 0

        val meta = new ShowMetaData(STATION, CHANNEL)
        meta.showTitle = Some(TITLE)
        meta.sourceVideoUrl = Some(FILE.toURI.toURL)

        showProcessingActor ! meta

        s3Backend.list().length mustEqual 1
      }
    }
  }

}
