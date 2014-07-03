import java.io.{File, FileOutputStream}

import actors.StorageBackendActor
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit._
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.AmazonS3Exception
import helper.{S3Backend, StorageMedia}
import org.specs2.matcher.ThrownMessages
import org.specs2.mutable.SpecificationLike
import play.api.Play
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.util.{Failure, Success}

/**
 * Specification for the backend upload actor. Test that the actor can store, retrieve and delete a file.
 *
 * @author Matthias L. Jugel
 */
class StorageBackendActorSpec extends TestKit(ActorSystem("test")) with SpecificationLike with ImplicitSender with ThrownMessages{
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

  "StorageBackendActor" should {
    "be able to store a media file" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)

        val storageActor = TestActorRef(new StorageBackendActor(new S3Backend(credentials, BUCKET)))

        val response = storageActor ? (("store", StorageMedia(STATION, CHANNEL, MEDIA, Some(FILE))))

        response.value match {
          case Some(Success(media: StorageMedia)) =>
            media.station must beEqualTo(STATION)
            media.channel must beEqualTo(CHANNEL)
            media.media must beEqualTo(MEDIA)
          case Some(Success(x))  =>
            fail("wrong response received: %s".format(x))
          case Some(Failure(e: Throwable)) =>
            fail("exception: %s".format(e))
          case None =>
            fail("no response received")
        }
      }
    }
    "be able to retrieve a media file" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)

        val storageActor = TestActorRef(new StorageBackendActor(new S3Backend(credentials, BUCKET)))

        val response = storageActor ? (("retrieve", StorageMedia(STATION, CHANNEL, MEDIA, Some(FILE))))

        response.value match {
          case Some(Success(media: StorageMedia)) =>
            media.station must beEqualTo(STATION)
            media.channel must beEqualTo(CHANNEL)
            media.media must beEqualTo(MEDIA)
          case Some(Success(x))  =>
            fail("wrong response received: %s".format(x))
          case Some(Failure(e: Throwable)) =>
            fail("exception: %s".format(e))
          case None =>
            fail("no response received")
        }
      }
    }
    "be able to delete a media file" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)

        val storageActor = TestActorRef(new StorageBackendActor(new S3Backend(credentials, BUCKET)))

        val media = StorageMedia(STATION, CHANNEL, MEDIA)
        storageActor ! (("delete", media))

        val s3 = new AmazonS3Client(credentials)
        try {
          s3.getObjectMetadata(BUCKET, media.toString)
          fail("object still exists: %s".format(media))
        } catch {
          case e: AmazonS3Exception => e.getStatusCode must be equalTo 404
        }
      }
    }
  }
}
