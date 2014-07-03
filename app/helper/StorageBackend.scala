package helper

import java.io.File
import java.util.UUID

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest

import scala.collection.JavaConverters._

/**
 * Simple storage media descriptor class.
 *
 * @author Matthias L. Jugel
 */
case class StorageMedia(station: String, channel: String, media: String, file: Option[File] = None) {
  override def toString = "%s/%s/%s".format(station, channel, media)
}

/**
 * Generic storage backend trait defining the functionality for a backend.
 */
trait StorageBackend {
  /**
   * Stora a media file in the backend.
   * @param media the media descriptor to store, requires the file portion to be set
   * @return the storage media if successful
   */
  def store(media: StorageMedia): StorageMedia

  /**
   * Retrieve a media file from the backend.
   *
   * @param media the media descriptor without the filled in file or it would be used to overwrite the contents
   * @return the media file with a filled in file part that contains the contents
   */
  def retrieve(media: StorageMedia): StorageMedia

  /**
   * Delete a media from the backend.
   *
   * @param media the media descriptor to delete in the backend
   */
  def delete(media: StorageMedia)

  /**
   * List backend storage contents.
   *
   * @return a list of media descriptors
   */
  def list(): List[StorageMedia]
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

  override def store(media: StorageMedia) = try {
    s3.putObject(bucket, media.toString, media.file.get)
    media
  } catch {
    case e: Exception => throw new StorageException("can't store %s".format(media), e)
  }

  override def retrieve(media: StorageMedia): StorageMedia = try {
    val target = media.file match {
      case Some(file: File) if file.exists && file.canWrite => file
      case None => File.createTempFile(UUID.randomUUID().toString, ".tmp")
    }
    val getObjectRequest = new GetObjectRequest(bucket, media.toString)
    s3.getObject(getObjectRequest, target)
    StorageMedia(media.station, media.channel, media.media, Some(target))
  } catch {
    case e: Exception => throw new RetrieveException("can't retrieve %s".format(media), e)
  }

  override def delete(media: StorageMedia) = try {
    s3.deleteObject(bucket, media.toString)
  } catch {
    case e: Exception => throw new DeleteException("can't delete %s".format(media), e)
  }

  override def list() = try {
    s3.listObjects(bucket).getObjectSummaries.asScala.map {
      s =>
        val Array(station, channel, media) = s.getKey.split("/")
        StorageMedia(station, channel, media)
    }.toList
  } catch {
    case e: Exception => throw new StorageException("can't retrieve file list", e)
  }
}
