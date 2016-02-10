package models

import play.api.libs.json._

/**
  * author: cvandrei
  * since: 2016-02-10
  */
case class MongoId(id: String) {

  override def toString = id

  def toJson: JsString = {
    JsString(id)
  }
}

object MongoId {

  def createReads: Reads[MongoId] = {
    __.read[String].map {
      l => MongoId(l)
    }
  }

  implicit def mongoReads: Reads[MongoId] =
    (__ \ 'mongoId).read[String].map {
      l => MongoId(l)
    }

  implicit def mongoWrites: Writes[MongoId] = Writes {
    id => Json.obj("mongoId" -> id.id)
  }
}
