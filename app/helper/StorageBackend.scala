package helper

import java.io.{FileNotFoundException, File}
import java.net.URL
import java.util.UUID

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest

import scala.collection.JavaConverters._

/**
 * Generic storage backend trait defining the functionality for a backend.
 */
trait StorageBackend {
  /**
   * Stora a media file in the backend.
   * @param name the media file name on the backend
   * @param file the media file to be stored
   * @return the storage media if successful
   */
  def store(name: String, file: File): URL

  /**
   * Retrieve a media file from the backend.
   *
   * @param name the file name on the backend
   * @return the media file as a local file
   */
  def retrieve(name: String, file: Option[File] = None): File

  /**
   * Delete a media from the backend.
   *
   * @param name the name of the file on the backend
   * @return true for successful deletion
   */
  def delete(name: String)

  /**
   * List backend storage contents.
   *
   * @return a list of media file names on the backend
   */
  def list(): List[String]
}

// Storage exceptions (wrap original exception)
class StorageException(m: String, t: Throwable) extends Exception(m, t)

class RetrieveException(m: String, t: Throwable) extends Exception(m, t)

class DeleteException(m: String, t: Throwable) extends Exception(m, t)

/**
 * A S3 storage backend that stores the media files on S3 using the path returned by StorageMedia.toString
 * as the key. This backend requires credentials and a bucket name to work.
 *
 * @param credentials the S3 credentials to use
 * @param bucket the bucket name
 */
class S3Backend(credentials: AWSCredentials, bucket: String) extends StorageBackend {
  private val s3 = new AmazonS3Client(credentials)

  override def store(name: String, file: File) = try {
    s3.putObject(bucket, name, file)
    s3.getUrl(bucket, name)
  } catch {
    case e: Exception => throw new StorageException("can't store %s".format(name), e)
  }

  override def retrieve(name: String, file: Option[File] = None): File = try {
    val localFile = file match {
      case Some(file: File) if file.exists && file.canWrite => file
      case None => File.createTempFile(UUID.randomUUID().toString, ".tmp")
    }
    val getObjectRequest = new GetObjectRequest(bucket, name)
    s3.getObject(getObjectRequest, localFile)
    localFile
  } catch {
    case e: Exception => throw new RetrieveException("can't retrieve %s".format(name), e)
  }

  override def delete(name: String) = try {
    s3.deleteObject(bucket, name)
  } catch {
    case e: Exception => throw new DeleteException("can't delete %s".format(name), e)
  }

  override def list() = try {
    s3.listObjects(bucket).getObjectSummaries.asScala.map(_.getKey).toList
  } catch {
    case e: Exception => throw new StorageException("can't retrieve file list", e)
  }
}
