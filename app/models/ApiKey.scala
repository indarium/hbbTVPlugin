package models

import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by dermicha on 06/09/14.
 */
case class ApiKey(apiKey: String)

object ApiKey {

  val showsCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("apikeys")

  implicit val format = Json.format[ApiKey]

  def checkApiKey(apiKey: String) = {
    showsCollection.
      // find all people with name `name`
      find(
        Json.obj(
          "apiKey" -> apiKey
        ),
        Json.obj()
      ).
      cursor[JsObject].collect[List](1).map { apiKey =>
      apiKey.headOption.map { currentApiKey =>
        currentApiKey.as[ApiKey]
      }
    }
  }
}
