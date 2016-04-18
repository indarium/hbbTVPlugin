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

  def hmsBroadcastUrl: String = Play.configuration.getString("hms.apiBroadcastURL").get

  def hmsTranscodeUrl: String = Play.configuration.getString("hms.apiTranscodeURL").get

  def hmsMinFileSize: Long = Play.configuration.getLong("hms.minFileSize").get

  def hmsCrawlerPeriod: Int = Play.configuration.getInt("hms.crawler.period").get

  def hmsLocalDownload(source: URL): String = Play.configuration.getString("hms.localDownload").getOrElse(source.toString)

  def hmsEncodingProfile: String = Play.configuration.getString("hms.encoding.profile").get

  def hmsEncodingCallbackUrl: String = Play.configuration.getString("hms.encoding.callbackUrl").get

  def hmsEncodingNotificationFinished: Boolean = Play.configuration.getBoolean("hms.encoding.notification.finished").get

  def hmsEncodingNotificationError: Boolean = Play.configuration.getBoolean("hms.encoding.notification.error").get

  def hmsEncodingNotificationStatus: Boolean = Play.configuration.getBoolean("hms.encoding.notification.status").get

  def hmsTranscoderActivateGlobal: Boolean = Play.configuration.getBoolean("hms.transcoder.activate.global").getOrElse(false)

  def hmsTranscoderActivateChannels: Array[String] = stringArray("hms.transcoder.activate.channels")

  def hmsTranscoderDeactivateChannels: Array[String] = stringArray("hms.transcoder.deactivate.channels")

  def hmsTranscodeStatusUpdateInterval: Long = Play.configuration.getLong("hms.transcoder.status.update.interval").getOrElse(300)

  def hmsImportAllShows: Array[String] = stringArray("hms.import.all.shows")

  /* WEBJAZZ CONFIGS **************************************************************************************************/

  def webjazzToken: String = Play.configuration.getString("webjazz.auth-token").getOrElse("NO-ACCESS-TOKEN")

  def webjazzUrl: String = Play.configuration.getString("webjazz.url").getOrElse("http://mmv-mediathek.de/import/vimeo.php")

  def webjazzNotifyActivateGlobal: Boolean = Play.configuration.getBoolean("webjazz.notify.activate.global").getOrElse(false)

  def webjazzNotifyActivateChannels: Array[String] = stringArray("webjazz.notify.activate.channels")

  def webjazzNotifyDeactivateChannels: Array[String] = stringArray("webjazz.notify.deactivate.channels")

  /* AWS CONFIG *******************************************************************************************************/

  def awsAccessKeyId: String = Play.configuration.getString("aws.accessKeyId").getOrElse("NO-ACCESS-KEY")

  def awsSecretKey: String = Play.configuration.getString("aws.secretKey").getOrElse("NO-SECRET-KEY")

  def awsBucket: String = Play.configuration.getString("aws.bucket").get

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

  def vimeoActivateGlobal: Boolean = Play.configuration.getBoolean("vimeo.activate.global").getOrElse(false)

  def vimeoActivateChannels: Array[String] = stringArray("vimeo.activate.channels")

  def vimeoDeactivateChannels: Array[String] = stringArray("vimeo.deactivate.channels")

  def vimeoEmbedPreset: String = Play.configuration.getString("vimeo.embed.preset").getOrElse("120222469")

  /* DOWNLOAD QUEUE AND RETRY CONFIGS *******************************************************************************************/

  def downloadQueueStartDelay: Long = Play.configuration.getLong("download.queue.delay.start").getOrElse(120L)

  def downloadQueueRetryInterval: Long = Play.configuration.getLong("download.queue.retry.interval").getOrElse(10L)

  def downloadRetryFirst: Int = Play.configuration.getInt("download.retry.first").getOrElse(60)

  def downloadRetryAfterFirst: Int = Play.configuration.getInt("download.retry.afterFirst").getOrElse(300)

  def downloadRetryMax: Int = Play.configuration.getInt("download.retry.max").getOrElse(5)

  def downloadParallelMax: Int = Play.configuration.getInt("download.parallel.max").getOrElse(1)

  /**
    *
    * @param key configuration key
    * @return empty array if nothing is found
    */
  private def stringArray(key: String): Array[String] = {

    val config: Option[String] = Play.configuration.getString(key)

    config match {
      case None => Array.empty
      case Some(value: String) if value.trim == "" => Array.empty
      case Some(value: String) => value.split(",").map(_.trim.toLowerCase)
    }

  }

}
