package helper

import java.net.{MalformedURLException, URL}

import com.amazonaws.auth.BasicAWSCredentials
import models.{DeleteShow, Show}

/**
  * Created by cvandrei on 2016-03-05.
  */
object S3Util {

  val awsAccessKeyId: String = Config.awsAccessKeyId
  val awsSecretKey: String = Config.awsSecretKey
  val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)

  def backend: S3Backend = new S3Backend(credentials, Config.awsBucket)

  /**
    * @param show basis for fileName extraction
    * @return extracted fileName
    * @throws MalformedURLException failed to extract fileName
    */
  def extractS3FileName(show: Show): String = new URL(show.showVideoSDUrl)
    .getPath
    .replaceFirst("/", "")

  /**
    * @param show basis for fileName extraction
    * @return extracted fileName
    * @throws MalformedURLException failed to extract fileName
    */
  def extractS3FileName(show: DeleteShow): String = new URL(show.showVideoSDUrl)
    .getPath
    .replaceFirst("/", "")

}
