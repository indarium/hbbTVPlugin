import java.io.{File, FileOutputStream}

import actors.StorageBackendActor
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit._
import com.amazonaws.auth.BasicAWSCredentials
import helper.{S3Backend, StorageMedia}
import org.specs2.mutable.SpecificationLike
import play.api.Play
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.util.Success

/**
 * >>Describe Class<<
 *
 * @author Matthias L. Jugel
 */
class StorageBackendActorSpec extends TestKit(ActorSystem("test")) with SpecificationLike with ImplicitSender {
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
  private val MEDIA = "Ulala"

  /*"StorageBackendActor" should {
    "store a media file" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)

        val storageActor = TestActorRef(new StorageBackendActor(new S3Backend(credentials, BUCKET)))

        val response = storageActor ? (("store", StorageMedia(STATION, CHANNEL, MEDIA, Some(FILE))))

        val Success(media: StorageMedia) = response.value.get

        media.station must beEqualTo(STATION)
        media.channel must beEqualTo(CHANNEL)
        media.media must beEqualTo(MEDIA)
      }
    }

  }*/
}
