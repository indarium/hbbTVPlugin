package models

import play.api.libs.json.Json
import reactivemongo.bson._

/**
  * Mapping Mongo's ObjectId("<some-hex-value>") as mentioned here: https://stackoverflow.com/questions/26840173/map-mongodb-id-using-play-reactivemongo-plugin?rq=1
  *
  * author: cvandrei
  * since: 2016-02-12
  */
case class MongoId($oid: String)

object MongoId {

  implicit val idFormat = Json.format[MongoId]

  def generate: String = BSONObjectID.generate.stringify

}
