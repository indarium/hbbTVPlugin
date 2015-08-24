package helper

import java.io.{File, FileOutputStream}

import play.api.Logger
import play.api.libs.ws.{WSResponseHeaders, WS, WSRequestHolder}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by dermicha on 23/08/15.
 **/
object AuthDownloader {

  private var testMode: Boolean = play.api.Play.isDev(play.api.Play.current)

  def downloadFile(source: String, outputFile: File): Future[Option[File]] = {

    Logger.debug(s"try to start download: $source / ${outputFile.getCanonicalPath}")

    import play.api.libs.iteratee._

    HMSApi.wsAuthRequest(source).flatMap {
      case Some(requestHolder: WSRequestHolder) =>
        val futureResponse: Future[(WSResponseHeaders, Enumerator[Array[Byte]])] = requestHolder.withRequestTimeout(900000).getStream()
        futureResponse.flatMap {
          case (headers, body) =>
            val outputStream = new FileOutputStream(outputFile)
            // The iteratee that writes to the output stream
            val iteratee = Iteratee.foreach[Array[Byte]] { bytes =>
              outputStream.write(bytes)
            }
            // Feed the body into the iteratee
            Logger.info(s"Starting download for: $source")
            (body |>>> iteratee).andThen {
              case result =>

                // Close the output stream whether there was an error or not
                outputStream.close()
                // Get the result or rethrow the error
                result.get
                Logger.info(s"Download successfull: $source")
            }.map(_ => Some(outputFile))
          case _ =>
            Future(None)
        }
      case _ =>
        Logger.error(s"could not download file: $source")
        throw new Exception(s"could not download file: $source")
        Future(None)
    }
  }
}
