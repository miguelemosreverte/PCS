package api.actor_transaction

import scala.concurrent.ExecutionContextExecutor
import akka.actor.ActorSystem
import akka.http.Controller
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
        log.info(s"${actorTransaction.topic} transaction stopped.")
        killswitch.shutdown()
        log.info("Setting currentTransaction to None")

        None
      case None =>
        log.info(s"${actorTransaction.topic} transaction was already stopped!")
        None
    }
  }

  def startTransaction(): Option[UniqueKillSwitch] = {
    def topic = actorTransaction.topic
    val transaction = actorTransaction.transaction _
    log.info(s"Starting ${actorTransaction.topic} transaction")
    val (killSwitch, done) = new KafkaTransactionalMessageProcessor(requirements)
      .run(topic, s"${topic}SINK", message => {
        transaction(message).map { output =>
          Seq(output.toString)
        }
      })
    done.onComplete { result =>
      // restart if the flag is set to true
      if (shouldBeRunning) {
        log.info(s"Transaction finished with $result. Restarting it.")
        stopTransaction()
        startTransaction()
      }
    }
    log.info("Setting currentTransaction to Some(killswitch)")
    currentTransaction = killSwitch
    killSwitch
  }

  def start_kafka: Route = {
    post {
      pathPrefix("start") {
        path(actorTransaction.topic) {
          handleErrors(exceptionHandler) {
            shouldBeRunning = true
            startTransaction()
            requests.increment()
            complete(s"Starting ${actorTransaction.topic} transaction \n ")
          }
        }
      }
    }
  }

  def stop_kafka: Route =
    post {
      pathPrefix("stop") {
        path(actorTransaction.topic) {
          handleErrors(exceptionHandler) {
            shouldBeRunning = false
            stopTransaction()
            requests.increment()
            complete(s"Stopping ${actorTransaction.topic} transaction \n ")
          }
        }
      }
    }

  def route: Route =
    pathPrefix("kafka") {
      start_kafka ~ stop_kafka
    }
}
