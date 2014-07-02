import java.io.{File, FileOutputStream}

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.AmazonS3Exception
import helper.{S3Backend, StorageMedia}
import org.junit.runner.RunWith
import org.specs2.matcher.ThrownMessages
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import play.api.Play
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.collection.JavaConverters._
import scala.io.Source


/**
 * >>Describe Class<<
 *
 * @author Matthias L. Jugel
 */
@RunWith(classOf[JUnitRunner])
class StorageBackendSpec extends Specification with ThrownMessages {
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

  "AWS S3" should {
    "have a bucket" in {
      running(FakeApplication()) {
        import play.api.Play.current

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

        val media = StorageMedia(STATION, CHANNEL, MEDIA, Some(FILE))
        backend.store(media)

        val s3 = new AmazonS3Client(credentials)
        val result = s3.getObject(BUCKET, media.toString)
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

        val media = StorageMedia(STATION, CHANNEL, MEDIA)
        val result = backend.retrieve(media)

        val content = Source.fromFile(result.file.get).mkString

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

        val media = StorageMedia(STATION, CHANNEL, MEDIA)
        backend.delete(media)

        val s3 = new AmazonS3Client(credentials)
        try {
          s3.getObjectMetadata(BUCKET, media.toString)
          fail("object still exists: %s".format(media))
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

        for(n <- 1 to 10) {
          backend.store(StorageMedia(STATION, CHANNEL, MEDIA + n, Some(FILE)))
        }

        val mediaFiles = backend.list()

        mediaFiles.length mustEqual 10
      }
    }
  }
}
