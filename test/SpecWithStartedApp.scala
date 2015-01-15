import org.specs2.mutable.BeforeAfter
import play.api.Play
import play.api.test.{FakeApplication, PlaySpecification}

/**
 * User: Björn Reimer
 * Date: 14.01.15
 * Time: 11:52
 */
trait SpecWithStartedApp extends PlaySpecification with BeforeAfter {

  override def before = {
    val app = FakeApplication()
    // check if app is started. start it if not
    Play.maybeApplication match {
      case Some(a) =>
      case None =>
        Play.start(app)
    }
  }

  override def after = {}
}
