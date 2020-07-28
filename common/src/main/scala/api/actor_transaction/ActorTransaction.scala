package api.actor_transaction

import scala.concurrent.Future
import akka.http.scaladsl.server.Route
import monitoring.Monitoring
import org.slf4j.LoggerFactory

abstract class ActorTransaction(monitoring: Monitoring) {
  val topic: String
  def transaction(input: String): Future[akka.Done]

  private val log = LoggerFactory.getLogger(this.getClass)

  def routeClassic(implicit system: akka.actor.ActorSystem): Route = {
    kafka.AtomicKafkaController(this, monitoring)(system).route
  }
  def route(implicit system: akka.actor.typed.ActorSystem[_]): Route = {
    kafka.AtomicKafkaController.fromTyped(this, monitoring)(system).route
  }
}
