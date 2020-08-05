package kafka

import akka.actor.ActorSystem
import akka.http.Controller
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import akka.stream.UniqueKillSwitch
import api.actor_transaction.ActorTransaction

import scala.concurrent.ExecutionContextExecutor

object AtomicKafkaController {

  case class AtomicKafkaController(actorTransaction: ActorTransaction[_],
                                   requirements: KafkaMessageProcessorRequirements)(
      implicit system: ActorSystem
  ) extends Controller(requirements.monitoring) {

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
    def startTransaction(): Unit = {
      val topic = actorTransaction.topic
      val transaction = actorTransaction.transaction _
      log.debug(s"Starting ${actorTransaction.topic} transaction")
      val (killSwitch, done) = new KafkaTransactionalMessageProcessor(requirements)
        .run(topic, s"${topic}SINK", message => {
          transaction(message).map { output: akka.Done =>
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
      currentTransaction = Some(killSwitch)
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
              complete(s"Stopping ${actorTransaction.topic} transaction \\n ")
            }
          }
        }
      }

    def route: Route =
      pathPrefix("kafka") {
        start_kafka ~ stop_kafka
      }

  }

}
