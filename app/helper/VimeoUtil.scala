package helper

/**
  * Created by cvandrei on 2016-03-05.
  */
object VimeoUtil {

  val vimeoAccessToken: String = Config.vimeoAccessToken

  def backend: VimeoBackend = new VimeoBackend(vimeoAccessToken)

}
