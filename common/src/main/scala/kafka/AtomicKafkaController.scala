package kafka

import scala.concurrent.ExecutionContextExecutor
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix, post, _}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.UniqueKillSwitch
import akka.{AkkaHttpServer, _}
import api.actor_transaction.ActorTransaction

object AtomicKafkaController {
  def fromTyped(
      actorTransaction: ActorTransaction
  )(implicit system: akka.actor.typed.ActorSystem[_]): AtomicKafkaController = {
    import akka.actor.typed.scaladsl.adapter._
    new AtomicKafkaController(actorTransaction)(system.toClassic)
  }
}

case class AtomicKafkaController(actorTransaction: ActorTransaction)(implicit system: ActorSystem)
    extends AkkaHttpServer {

  implicit private val ec: ExecutionContextExecutor = system.dispatcher

  implicit private val transactionRequirements: KafkaMessageProcessorRequirements =
    KafkaMessageProcessorRequirements.productionSettings()
  val requirements = StopStartKafka.StartStopKafkaRequirements(actorTransaction, transactionRequirements)
  val actorTransactionController: ActorRef =
    StopStartKafkaActor.startWithRequirements(
      requirements
    )

  var killSwitches: UniqueKillSwitch = _
  var isKafkaStarted: Boolean = false
  val topic: String = requirements.actorTransactions.topic

  def startTransaction(): UniqueKillSwitch = {
    log.info(s"Starting transactional consumer for $topic")
    val (killSwitch, done) = new KafkaTransactionalMessageProcessor()
      .run(topic, s"${topic}SINK", message => {
        requirements.actorTransactions.transaction(message).map { output: akka.Done =>
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
          for {
            _ <- actorTransactionController.ask[akka.Done](StopStartKafkaActor.StartKafka())
          } yield akka.Done

          killSwitches = startTransaction()
          isKafkaStarted = true
          complete("Starting Kafka")
        }
      }
    }
  }

  def stop_kafka: Route =
    post {
      pathPrefix("stop") {
        path(actorTransaction.topic) {
          for {
            _ <- actorTransactionController.ask[akka.Done](StopStartKafkaActor.StopKafka())
          } yield akka.Done

          killSwitches.shutdown()
          log.info("Stopped Kafka")
          killSwitches = null
          isKafkaStarted = false

          complete("Stopping Kafka")
        }
      }
    }

  def routes: Route =
    pathPrefix("kafka") {
      start_kafka ~ stop_kafka
    }

}
