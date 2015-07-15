/**
 * >>Describe Class<<
 *
 * @author Matthias L. Jugel
 */

import java.io.File

import actors._
import akka.actor.Props
import com.fasterxml.jackson.core.JsonParseException
import play.api._
import play.api.libs.json.{JsResultException, JsValue, JsObject, Json}
import play.libs.Akka
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
 import play.api.Play.current
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {

    // load initial data, if not present
    if(Play.configuration.getBoolean("startup.initialData.load").getOrElse(false)) {
      // get all files in fixtures

      new File(Play.application.path.getAbsolutePath + "/fixtures/").listFiles.toSeq.map {
        file =>
          if (file.getName.endsWith(".json")) {
            Logger.info("Loading fixture: " + file.getName)

            // use file name as collection name
            val colName = file.getName.replace(".json", "")
            val col = ReactiveMongoPlugin.db.collection[JSONCollection](colName)

            // check if the collection is empty
            col.find(Json.obj()).one[JsValue].map {
              case Some(js) => // do nothing
              case None =>
                // parse file as json and insert into db
                try{
                  val jsonString = Source.fromFile(file).mkString
                  Json.parse(jsonString).as[Seq[JsObject]].foreach(col.insert)
                } catch {
                  case e: JsonParseException => Logger.error("Error parsing fixture: " + file.getAbsolutePath, e)
                  case e: JsResultException => Logger.error("Not an array of json objects: " + file.getAbsolutePath, e)
                }
            }
          }
      }
    }

    // start show crawler
    val showCrawler = Akka.system.actorOf(Props(new ShowCrawler()))
    showCrawler ! new StartProcess
  }
}