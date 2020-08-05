package kafka

import scala.concurrent.ExecutionContextExecutor
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix, post, _}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.UniqueKillSwitch
import akka._
import akka.http.{AkkaHttpServer, Controller}
import api.actor_transaction.ActorTransaction
import monitoring.Monitoring

object AtomicKafkaController {

  case class AtomicKafkaController(actorTransaction: ActorTransaction[_],
                                   requirements: KafkaMessageProcessorRequirements)(
      implicit system: ActorSystem
  ) extends Controller(requirements.monitoring) {

    implicit private val ec: ExecutionContextExecutor = system.dispatcher

    var killSwitches: UniqueKillSwitch = _
    var isKafkaStarted: Boolean = false

    def startTransaction(): UniqueKillSwitch = {

      val topic = actorTransaction.topic
      val transaction = actorTransaction.transaction _

      val (killSwitch, done) = new KafkaTransactionalMessageProcessor(requirements)
        .run(topic, s"${topic}SINK", message => {
          transaction(message).map { output: akka.Done =>
            Seq(output.toString)
          }
        })
      done.onComplete { result =>
        if (isKafkaStarted) {
          // restart if the flag is set to true
          log.warn(s"Transaction finished with $result. Restarting it.")
          startTransaction()
        }
      }
      killSwitch
    }

    def start_kafka: Route = {
      post {
        pathPrefix("start") {
          path(actorTransaction.topic) {
            handleErrors(exceptionHandler) {
              log.info("Starting Kafka")
              killSwitches = startTransaction()
              isKafkaStarted = true
              requests.increment()
              complete("Starting Kafka")
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
              killSwitches.shutdown()
              log.info("Stopped Kafka")
              killSwitches = null
              isKafkaStarted = false
              requests.increment()
              complete("Stopping Kafka")
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
