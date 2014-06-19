package helper

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{IndexType, Index}
import play.api.Play.current

/**
 * Created by dermicha on 12/06/14.
 */
object MongoDB {

  val mongoDBConnection = ReactiveMongoPlugin.db

  lazy val channelCollection: JSONCollection = {
    val col = mongoDBConnection.collection[JSONCollection]("channels")
    //col.indexesManager.ensure(Index(Seq("" -> IndexType.Ascending)))
    col
  }

}
