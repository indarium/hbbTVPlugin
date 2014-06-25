package actors

import akka.actor.Actor
import akka.event.Logging
import play.api.Play
import play.api.Play.current

/**
 * Created by dermicha on 24/06/14.
 */
class AWS_S3_Upload extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case uploadJob: UploadJob => {
      log.info("start file upload")
      //aws s3 bucketname to store video data
      var s3Bucket = Play.configuration.getString("aws.bucket").get

      // https://github.com/Rhinofly/play-s3/tree/v5.0.0
      // or
      // Amazon Java AWS SDK ?
      // build.sbt: "com.amazonaws" % "aws-java-sdk" % "1.7.8.1",

      sender() ! ""
    }
    case _ => log.info("received unknown message")
  }

}

case class UploadJob(sourceFileId: String,
                     sourceFilePath: String,
                     stationId: String,
                     channelId: String)

case class S3File(sourceFileId: String,
                  s3FilePath: String)
