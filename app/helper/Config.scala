package helper

import java.net.URL

import play.api.Play
import play.api.Play.current

/**
  * author: cvandrei
  * since: 2016-02-10
  */
object Config {

  /* APPLICATION CONFIGS **********************************************************************************************/

  def startupInitialDataLoad: Boolean = Play.configuration.getBoolean("startup.initialData.load").getOrElse(false)

  def cdnBaseUrl = Play.configuration.getString("cdn.baseUrl").get

  /* HMS CONFIGS ******************************************************************************************************/

  def hmsUserName: String = Play.configuration.getString("hms.username").get

  def hmsPassword: String = Play.configuration.getString("hms.password").get

  def hmsApiUrl: String = Play.configuration.getString("hms.apiBroadcastURL").get

  def hmsBroadcastUrl: String = Play.configuration.getString("hms.apiBroadcastURL").get

  def hmsMinFileSize: Long = Play.configuration.getLong("hms.minFileSize").get

  def hmsCrawlerPeriod: Int = Play.configuration.getInt("hms.crawler.period").get

  def hmsLocalDownload(source: URL): String = Play.configuration.getString("hms.localDownload").getOrElse(source.toString)

  /* WEBJAZZ CONFIGS **************************************************************************************************/

  def webjazzToken: String = Play.configuration.getString("webjazz.auth-token").getOrElse("NO-ACCESS-TOKEN")

  def webjazzUrl: String = Play.configuration.getString("webjazz.url").getOrElse("http://mmv-mediathek.de/import/vimeo.php")

  /* VIMEO CONFIGS ****************************************************************************************************/

  /**
    * The interval in seconds in which we run VimeoEncodingStatusActor.
    *
    * @return number of seconds
    */
  def vimeoEncodingCheckInterval: Int = Play.configuration.getInt("vimeo.encoding.check-interval").getOrElse(120)

  /**
    * Gives us the access token for vimeo.
    *
    * @return "NO-ACCESS-TOKEN" if not configured
    */
  def vimeoAccessToken: String = Play.configuration.getString("vimeo.accessToken").getOrElse("NO-ACCESS-TOKEN")

  def vimeoEncodingBatchSize: Int = Play.configuration.getInt("vimeo.encoding.batch.size").getOrElse(10)

}
