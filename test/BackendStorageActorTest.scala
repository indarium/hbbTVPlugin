import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

import actors.StorageBackendActor
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import org.junit.Assert._
import org.junit._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * >>Describe Class<<
 *
 * @author Matthias L. Jugel
 */
class BackendStorageActorTest {
 /* val s3credentials = new AWSCredentials {
    def getAWSAccessKeyId = System.getProperty("aws.accessKeyId", "NONE")

    def getAWSSecretKey = System.getProperty("aws.secretKey", "NONE")
  }

  val testBucketName: String = UUID.randomUUID().toString.toLowerCase

  @Before
  def createBucket() {
    val s3 = new AmazonS3Client(s3credentials)
    s3.createBucket(testBucketName)
  }

  @After
  def removeBucket() {
    val s3 = new AmazonS3Client(s3credentials)
    s3.deleteBucket(testBucketName)
  }

  @Test
  def uploadTest() {
    implicit val system = ActorSystem("MyActorSystem")
    implicit val ec = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
    implicit val timeout = Timeout(5 seconds)

    val s3UploadActor = TestActorRef(new StorageBackendActor(s3credentials, testBucketName))
    val testFile = File.createTempFile("testdata.", ".tmp")

    val result = s3UploadActor ? UploadJob("ID123", testFile, "station-5", "channel-5")

    result.value.get match {
      case Success(s3file: S3File) => assertEquals("", s3file)
      case Failure(t: Throwable) => fail("upload failed: %s".format(t.getMessage))
      case _ => fail("unknown response received")
    }
  }*/
}
