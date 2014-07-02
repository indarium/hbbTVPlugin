package helper

import java.io.File
import java.util.UUID

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest

import scala.collection.JavaConverters._

/**
 * >>Describe Class<<
 *
 * @author Matthias L. Jugel
 */
case class StorageMedia(station: String, channel: String, media: String, file: Option[File] = None) {
  override def toString = "%s/%s/%s".format(station, channel, media)
}

trait StorageBackend {

  def store(media: StorageMedia): StorageMedia

  def retrieve(media: StorageMedia): StorageMedia

  def delete(media: StorageMedia)

  def list(): List[StorageMedia]
}

class StorageException(m: String, t: Throwable) extends Exception(m, t)

class RetrieveException(m: String, t: Throwable) extends Exception(m, t)

class DeleteException(m: String, t: Throwable) extends Exception(m, t)


class S3Backend(credentials: AWSCredentials, bucket: String) extends StorageBackend {
  private val s3 = new AmazonS3Client(credentials)

  override def store(media: StorageMedia) = try {
    s3.putObject(bucket, media.toString, media.file.get)
    media
  } catch {
    case e: Exception => throw new StorageException("can't store %s".format(media), e)
  }

  override def retrieve(media: StorageMedia): StorageMedia = try {
    val target = File.createTempFile(UUID.randomUUID().toString, ".tmp")
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
