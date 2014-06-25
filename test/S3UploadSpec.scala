import actors.{UploadJob, AWS_S3_Upload}
import akka.testkit.TestActorRef
import akka.actor.ActorSystem
import java.io.File
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.test.WithApplication

/**
 * >>Describe Class<<
 *
 * @author Matthias L. Jugel
 */
@RunWith(classOf[JUnitRunner])
object S3UploadSpec extends Specification {
  implicit val system = ActorSystem("MyActorSystem")
  implicit val s3UploadActor = TestActorRef[AWS_S3_Upload]
  implicit val testFile = File.createTempFile("testdata", ".tmp")


  "S3Upload" should {
    "send file to s3" in new WithApplication {



      s3UploadActor ! UploadJob("ID123", testFile, "station-5", "channel-5")

      1 === 1
    }
  }
}
