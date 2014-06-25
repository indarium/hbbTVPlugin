package actors

import akka.actor.Actor
import akka.event.Logging
import play.api.Play
import play.api.Play.current
import java.io.File
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.model.PutObjectRequest


/**
 * Created by dermicha on 24/06/14.
 */
class AWS_S3_Upload extends Actor {
  val log = Logging(context.system, this)

  val s3credentials = new AWSCredentials {
    def getAWSAccessKeyId = Play.configuration.getString("aws.accessKeyId").getOrElse("")

    def getAWSSecretKey = Play.configuration.getString("aws.accessSecretKey").getOrElse("")
  }
  val s3client = new AmazonS3Client(s3credentials)

  def receive = {
    case UploadJob(sourceFileId, sourceFile, stationId, channelId) => {
      log.info("start file upload")
      //aws s3 bucketname to store video data
      val s3Bucket = Play.configuration.getString("aws.bucket").get
      val s3FilePath = "%s/%s/%s".format(stationId, channelId, sourceFileId)

      val s3response = s3client.putObject(new PutObjectRequest(s3Bucket, s3FilePath, sourceFile))
      log.debug(s3response.toString)

      sender() ! S3File(sourceFileId, s3FilePath)
    }
    case _ => log.info("received unknown message")
  }

}

case class UploadJob(sourceFileId: String, sourceFile: File, stationId: String, channelId: String)

case class S3File(sourceFileId: String, s3FilePath: String)


