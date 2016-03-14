package helper.vimeo

import helper.Config

/**
  * Created by cvandrei on 2016-03-14.
  */
object VimeoUtil {

  def uploadActivated(stationId: String): Boolean = {

    val stationIdLowerCase = stationId.toLowerCase
    Config.vimeoActivateGlobal match {
      case true => !Config.vimeoDeactivateChannels.contains(stationIdLowerCase)
      case false => Config.vimeoActivateChannels.contains(stationIdLowerCase)
    }

  }

}
