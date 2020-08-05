package api.actor_transaction

import akka.Done
import akka.http.scaladsl.server.Route
import akka.stream.UniqueKillSwitch
import kafka.{KafkaMessageProcessorRequirements, KafkaTransactionalMessageProcessor}
import monitoring.Monitoring
import play.api.libs.json.Format
import serialization.decodeF

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class ActorTransaction[ExternalDto](
    monitoring: Monitoring
)(implicit ec: ExecutionContext, format: Format[ExternalDto], c: ClassTag[ExternalDto])
    extends ActorTransactionMetrics(monitoring) {
  val topic: String

  final def transaction(input: String): Future[akka.Done] = {
    val future = for {
      cmd <- processInput(input)
      done <- processCommand(cmd)
    } yield done

    recordRequests(future)
    recordErrors(future)
    recordLatency(future)
    future
  }

  final def processInput(input: String): Future[ExternalDto] =
    Future(decodeF[ExternalDto](input))

  def processCommand(registro: ExternalDto): Future[Done]

}

object ActorTransaction {
  object Implicits {
    implicit class ActorTransactionRoute(actorTransaction: ActorTransaction[_])(
        implicit requirements: KafkaMessageProcessorRequirements
    ) {

      final def route(implicit system: akka.actor.ActorSystem): Route = {
        kafka.AtomicKafkaController.AtomicKafkaController(actorTransaction, requirements)(system).route
      }

    }

    implicit class ActorTransactionStart(actorTransaction: ActorTransaction[_])(
        implicit requirements: KafkaMessageProcessorRequirements,
        ec: ExecutionContext
    ) {

      final def start(
          requirements: KafkaMessageProcessorRequirements
      )(implicit system: akka.actor.ActorSystem): (UniqueKillSwitch, Future[Done]) = {
        val topic = actorTransaction.topic
        val transaction = actorTransaction.transaction _
        new KafkaTransactionalMessageProcessor(requirements)
          .run(topic, s"${topic}SINK", message => {
            transaction(message).map { output: akka.Done =>
              Seq(output.toString)
            }
          })
      }
    }
  }
}
