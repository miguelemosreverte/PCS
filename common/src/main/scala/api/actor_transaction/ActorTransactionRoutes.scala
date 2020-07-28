package api.actor_transaction

import akka.http.scaladsl.server.Route

trait ActorTransactionRoutes { self: ActorTransaction[_] =>
  final def routesClassic(implicit system: akka.actor.ActorSystem): Route = {
    kafka.AtomicKafkaController(this)(system).routes
  }
  final def routes(implicit system: akka.actor.typed.ActorSystem[_]): Route = {
    kafka.AtomicKafkaController.fromTyped(this)(system).routes
  }

}
