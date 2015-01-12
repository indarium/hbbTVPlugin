import java.io.{File, FileOutputStream}

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.AmazonS3Exception
import helper.{DeleteException, StorageException, S3Backend}
import org.junit.runner.RunWith
import org.specs2.matcher.ThrownMessages
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import play.api.Play
import play.api.Play.current
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.collection.JavaConverters._
import scala.io.Source

/**
 * Specification for the storage backend. Test that the backend can store, retrieve and delete a file.
 *
 * @author Matthias L. Jugel
 */
@RunWith(classOf[JUnitRunner])
class StorageBackendSpec extends Specification with ThrownMessages {
  sequential

  //private val BUCKET = "ac846539-6757-4284-a4d7-ce227d87a7ab"
  private val BUCKET = Play.configuration.getString("aws.bucket").getOrElse("NO-AWS-BUCKET")

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

  private val NAME = "%s/%s/%s".format(STATION, CHANNEL, MEDIA)

  "AWS S3" should {
    "have a bucket" in {
      running(FakeApplication()) {

        BUCKET must not be "NO-AWS-BUCKET"

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
        val s3 = new AmazonS3Client(credentials)

        s3.doesBucketExist(BUCKET) must beTrue
      }
    }

    "have an empty bucket" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
        val s3 = new AmazonS3Client(credentials)

        val objects = s3.listObjects(BUCKET)

        objects.isTruncated must beFalse

        objects.getObjectSummaries.asScala foreach (o => s3.deleteObject(BUCKET, o.getKey))

        s3.listObjects(BUCKET).getObjectSummaries.isEmpty must beTrue
      }
    }
  }

  "S3 Backend" should {
    "store a media file" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
        val backend = new S3Backend(credentials, BUCKET)

        backend.store(NAME, FILE)

        val s3 = new AmazonS3Client(credentials)
        val result = s3.getObject(BUCKET, NAME)
        val content = Source.fromInputStream(result.getObjectContent).mkString

        content must be equalTo CONTENT
      }
    }

    "retrieve a media file" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
        val backend = new S3Backend(credentials, BUCKET)

        val result = backend.retrieve(NAME)

        val content = Source.fromFile(result).mkString

        content must be equalTo CONTENT
      }
    }

    "delete a media file" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
        val backend = new S3Backend(credentials, BUCKET)

        backend.delete(NAME)

        val s3 = new AmazonS3Client(credentials)
        try {
          s3.getObjectMetadata(BUCKET, NAME)
          fail("object still exists: %s".format(NAME))
        } catch {
          case e: AmazonS3Exception => e.getStatusCode must be equalTo 404
        }
      }
    }

    "list media files" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
        val backend = new S3Backend(credentials, BUCKET)

        for (n <- 1 to 10) {
          backend.store(NAME + "-" + n, FILE)
        }

        val mediaFiles = backend.list()

        mediaFiles.length mustEqual 10

        for (n <- 1 to 10) {
          val content = Source.fromFile(backend.retrieve(NAME + "-" + n)).mkString
          content must be equalTo CONTENT
          backend.delete(NAME + "-" + n)
        }

        backend.list().length mustEqual 0
      }
    }

    "accept maximal 1024 character long file names" in {
      running(FakeApplication()) {
        import play.api.Play.current

        val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
        val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

        awsAccessKeyId must not be "NO-ACCESS-KEY"
        awsSecretKey must not be "NO-SECRET-KEY"

        val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
        val backend = new S3Backend(credentials, BUCKET)

        val longFileName = "X" * 1024
        backend.store(longFileName, FILE)

        val content = Source.fromFile(backend.retrieve(longFileName)).mkString
        content must be equalTo CONTENT

        val tooLongFileName = "X" * 1025
        backend.store(tooLongFileName, FILE) should throwA[StorageException]()

        backend.list().length mustEqual 1

        backend.delete(longFileName)
        backend.delete(tooLongFileName) should throwA[DeleteException]

        backend.list().length mustEqual 0
      }
    }
  }
}
