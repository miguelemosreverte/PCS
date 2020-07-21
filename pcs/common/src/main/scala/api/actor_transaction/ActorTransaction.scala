package api.actor_transaction

import scala.concurrent.Future

import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory

trait ActorTransaction {
  val topic: String
  def transaction(input: String): Future[akka.Done]

  private val log = LoggerFactory.getLogger(this.getClass)

  def routesClassic(implicit system: akka.actor.ActorSystem): Route = {
    log.info((system == null).toString)
    kafka.AtomicKafkaController(this)(system).routes
  }
  def routes(implicit system: akka.actor.typed.ActorSystem[_]): Route = {
    log.info((system == null).toString)
    kafka.AtomicKafkaController.fromTyped(this)(system).routes
  }
}
