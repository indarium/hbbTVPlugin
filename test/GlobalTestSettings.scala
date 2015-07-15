import java.io.File

import play.api._

/**
 * Created by dermicha on 03/04/15.
 **/

object GlobalTestSettings extends GlobalSettings {

  override def beforeStart(app: Application): Unit = {
    super.beforeStart(app)
  }

  override def onLoadConfig(config: Configuration,
                            path: File,
                            classloader: ClassLoader,
                            mode: Mode.Mode): Configuration = {
    config ++ configuration ++
      Configuration.load(path, mode = Mode.Dev,
        Map("config.file" -> "conf/application.test.conf"))
  }

  override def onStart(app: Application) {
  }

  override def onStop(app: Application) {

  }
}