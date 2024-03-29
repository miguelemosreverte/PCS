package api.actor_transaction

import scala.concurrent.ExecutionContextExecutor
import akka.actor.ActorSystem
import akka.http.Controller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import akka.stream.UniqueKillSwitch
import kafka.{KafkaMessageProcessorRequirements, KafkaPlainConsumerMessageProcessor, KafkaTransactionalMessageProcessor}

class ActorTransactionController(
    actorTransaction: ActorTransaction[_],
    requirements: KafkaMessageProcessorRequirements
) extends Controller(requirements.monitoring) {

  implicit val system = requirements.system
  implicit private val ec: ExecutionContextExecutor = system.dispatcher

  var currentTransaction: Option[UniqueKillSwitch] = None
  var shouldBeRunning: Boolean = false

  def stopTransaction(): Unit = {
    currentTransaction = currentTransaction match {
      case Some(killswitch) =>
        log.debug(s"${actorTransaction.topic} transaction stopped.")
        killswitch.shutdown()
        log.debug("Setting currentTransaction to None")

        None
      case None =>
        log.debug(s"${actorTransaction.topic} transaction was already stopped!")
        None
    }
  }

  def startTransaction(): Option[UniqueKillSwitch] = {
    def topic = actorTransaction.topic
    val transaction = actorTransaction.transaction _
    log.debug(s"Starting ${actorTransaction.topic} transaction")
    val (killSwitch, done) = new KafkaTransactionalMessageProcessor(requirements)
      .run(topic, s"${topic}SINK", message => {
        transaction(message).map { output =>
          Seq(output.toString)
        }
      })
    done.onComplete { result =>
      // restart if the flag is set to true
      if (shouldBeRunning) {
        log.debug(s"Transaction finished with $result. Restarting it.")
        stopTransaction()
        startTransaction()
      }
    }
    log.debug("Setting currentTransaction to Some(killswitch)")
    currentTransaction = killSwitch
    killSwitch
  }
  def route: Route =
    path("api" / "system" / "health" / "topic" / actorTransaction.topic) {
      complete(StatusCodes.OK)
    }
}
