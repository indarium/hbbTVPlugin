package actors

import akka.actor.{Actor, Props, Terminated}
import akka.routing.{Router, ActorRefRoutee, RoundRobinRoutingLogic}
import helper.ShowMetaData

/**
 * A simple round robin router.
 *
 * @author Matthias L. Jugel
 */
class GenericRouter(actor: Props, workers: Int = 5) extends Actor {
  var router = {
    val routees = Vector.fill(workers) {
      val r = context.actorOf(actor)
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = {
    case m: ShowMetaData =>
      router.route(m, sender())

    case Terminated(a) =>
      router = router.removeRoutee(a)
      val r = context.actorOf(actor)
      context watch r
      router = router.addRoutee(r)
  }
}
