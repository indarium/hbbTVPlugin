package helper

import com.amazonaws.auth.BasicAWSCredentials

/**
  * Created by cvandrei on 2016-03-05.
  */
object S3Util {

  val awsAccessKeyId: String = Config.awsAccessKeyId
  val awsSecretKey: String = Config.awsSecretKey
  val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)

  def backend: S3Backend = new S3Backend(credentials, Config.awsBucket)

}
