import java.io.{ File, FileOutputStream }
import java.net.URL

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.AmazonS3Exception
import helper._
import org.junit.runner.RunWith
import org.specs2.matcher.ThrownMessages
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import play.api.libs.ws.WS
import play.api.{ Logger, Play }
import play.api.test.{ WithApplication, PlaySpecification, FakeApplication }
import play.api.test.Helpers._

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.io.Source
import play.api.Play.current

/**
 * Specification for the storage backend. Test that the backend can store, retrieve and delete a file.
 *
 * @author Matthias L. Jugel
 */
@RunWith(classOf[JUnitRunner])
class StorageBackendSpec extends SpecWithStartedApp with ThrownMessages {
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

  val meta = new ShowMetaData(STATION, CHANNEL)
  meta.localVideoFile = Some(FILE)

  // TODO: Fix these
    "AWS S3" should {
      "have a bucket" in {
          import play.api.Play.current

          val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
          val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

          awsAccessKeyId must not be "NO-ACCESS-KEY"
          awsSecretKey must not be "NO-SECRET-KEY"


          val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
          val s3 = new AmazonS3Client(credentials)

          s3.doesBucketExist(BUCKET) must beTrue
      }

      "have an empty bucket" in {
          import play.api.Play.current

          val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
          val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

          awsAccessKeyId must not be "NO-ACCESS-KEY"
          awsSecretKey must not be "NO-SECRET-KEY"

          val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
          val s3 = new AmazonS3Client(credentials)

          val objects = s3.listObjects(BUCKET)

          Logger.debug("Objects in bucket: " + objects.getObjectSummaries.size)

          objects.isTruncated must beFalse

          objects.getObjectSummaries.asScala foreach (o => s3.deleteObject(BUCKET, o.getKey))

          s3.listObjects(BUCKET).getObjectSummaries.isEmpty must beTrue
        }
    }

    var s3FileName = ""
    "S3 Backend" should {
      "store a media file" in {
          import play.api.Play.current

          val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
          val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

          awsAccessKeyId must not be "NO-ACCESS-KEY"
          awsSecretKey must not be "NO-SECRET-KEY"

          val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
          val backend = new S3Backend(credentials, BUCKET)

          val url = backend.store(meta)
          s3FileName = url.getPath.substring(1)

          val s3 = new AmazonS3Client(credentials)

          val result = s3.getObject(BUCKET,s3FileName)
          val content = Source.fromInputStream(result.getObjectContent).mkString

          content must be equalTo CONTENT
        }

      "retrieve a media file" in {
          import play.api.Play.current

          val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
          val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

          awsAccessKeyId must not be "NO-ACCESS-KEY"
          awsSecretKey must not be "NO-SECRET-KEY"

          val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
          val backend = new S3Backend(credentials, BUCKET)

          val result = backend.retrieve(s3FileName)

          val content = Source.fromFile(result).mkString

          content must be equalTo CONTENT
      }

      "delete a media file" in {
          import play.api.Play.current

          val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
          val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

          awsAccessKeyId must not be "NO-ACCESS-KEY"
          awsSecretKey must not be "NO-SECRET-KEY"

          val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
          val backend = new S3Backend(credentials, BUCKET)

          backend.delete(s3FileName)

          val s3 = new AmazonS3Client(credentials)
          try {
            s3.getObjectMetadata(BUCKET, s3FileName)
            fail("object still exists: %s".format(s3FileName))
          } catch {
            case e: AmazonS3Exception => e.getStatusCode must be equalTo 404
          }
        }

//      "list media files" in {
//          import play.api.Play.current
//
//          val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
//          val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")
//
//          awsAccessKeyId must not be "NO-ACCESS-KEY"
//          awsSecretKey must not be "NO-SECRET-KEY"
//
//          val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
//          val backend = new S3Backend(credentials, BUCKET)
//
//          for (n <- 1 to 10) {
//            backend.store(NAME + "-" + n, FILE)
//          }
//
//          val mediaFiles = backend.list()
//
//          mediaFiles.length mustEqual 10
//
//          for (n <- 1 to 10) {
//            val content = Source.fromFile(backend.retrieve(NAME + "-" + n)).mkString
//            content must be equalTo CONTENT
//            backend.delete(NAME + "-" + n)
//          }
//
//          backend.list().length mustEqual 0
//      }
//
//      "accept maximal 1024 character long file names" in {
//          import play.api.Play.current
//
//          val awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")
//          val awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")
//
//          awsAccessKeyId must not be "NO-ACCESS-KEY"
//          awsSecretKey must not be "NO-SECRET-KEY"
//
//          val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
//          val backend = new S3Backend(credentials, BUCKET)
//
//          val longFileName = "X" * 1024
//          backend.store(longFileName, FILE)
//
//          val content = Source.fromFile(backend.retrieve(longFileName)).mkString
//          content must be equalTo CONTENT
//
//          val tooLongFileName = "X" * 1025
//          backend.store(tooLongFileName, FILE) should throwA[StorageException]()
//
//          backend.list().length mustEqual 1
//
//          backend.delete(longFileName)
//          backend.delete(tooLongFileName) should throwA[DeleteException]
//
//          backend.list().length mustEqual 0
//      }
    }

  "Vimeo backend should" should {

    // create testdata and initiate backend
    val testFile = new File("./testFiles/video_small.mp4")
    val meta = new ShowMetaData(STATION, CHANNEL)
    meta.localVideoFile = Some(testFile)
    meta.channelName = Some("test_channel")
    var videoUrl: URL = null

    Play.configuration.getString("vimeo.accessToken") must beSome
    val accessToken = Play.configuration.getString("vimeo.accessToken").get
    val backend = new VimeoBackend(accessToken)

    "Authenticate to vimeo API" in {
      backend.ping must beTrue
    }

    "Upload test video with metadata" in {
      videoUrl = backend.store(meta)
      Logger.debug("VideoURL: " + videoUrl)
      videoUrl.getPath must beMatching("^/\\d+")
    }

    "Delete test video" in {
      backend.delete(videoUrl.getPath.split("/").last)

      // todo: test if it was really deleted
      1 === 1
    }
  }
}
