import java.io.{File, FileOutputStream}
import java.net.URL

import actors.VideoDownloadActor
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import models.dto.{VideoDownloadFailure, VideoDownloadSuccess, ShowMetaData}
import org.specs2.matcher.ThrownMessages
import org.specs2.mutable.SpecificationLike

/**
 * >>Describe Class<<
 *
 * @author Matthias L. Jugel
 */
class VideoDownloadActorSpec extends TestKit(ActorSystem("test")) with SpecificationLike with ImplicitSender with ThrownMessages {
  sequential

  private val CONTENT = "THIS IS AN EXAMPLE FILE"
  private val FILE = File.createTempFile("upload.", ".tmp")
  FILE.deleteOnExit()

  private val os = new FileOutputStream(FILE)
  os.write(CONTENT.getBytes("UTF-8"))
  os.flush()
  os.close()

  private val LOCAL_FILE_URL = FILE.toURI.toURL

  private val STATION = "SPACE"
  private val CHANNEL = "CHANNEL-5"

  "VideoDownloadActor" should {
    "be able to download a file" in {
      val videoDownloadActor = TestActorRef(new VideoDownloadActor)

      val meta = new ShowMetaData(STATION, CHANNEL)
      meta.sourceVideoUrl = Some(LOCAL_FILE_URL)

      videoDownloadActor ! meta

      expectMsgType[VideoDownloadSuccess].meta.localVideoFile.isDefined must beTrue
    }

    "reply with a failure in case of an error" in {
      val videoDownloadActor = TestActorRef(new VideoDownloadActor)

      val meta = new ShowMetaData(STATION, CHANNEL)
      meta.sourceVideoUrl = Some(new URL("file:unknown-file"))

      videoDownloadActor ! meta

      expectMsgType[VideoDownloadFailure].meta.localVideoFile.isDefined must beFalse
    }

    "reply with a failure in case of a missing URL" in {
      val videoDownloadActor = TestActorRef(new VideoDownloadActor)

      val meta = new ShowMetaData(STATION, CHANNEL)

      videoDownloadActor ! meta

      expectMsgType[VideoDownloadFailure].meta.localVideoFile.isDefined must beFalse
    }
  }
}
