package api.actor_transaction

import java.time.LocalDateTime

import akka.Done
import akka.actor.Props
import akka.cluster.Cluster

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import akka.http.scaladsl.server.Route
import akka.stream.UniqueKillSwitch
import design_principles.actor_model.Response
import design_principles.threading.bulkhead_pattern.bulkheads.ActorBulkhead
import kafka.{KafkaMessageProcessorRequirements, KafkaTransactionalMessageProcessor, TopicListener}
import monitoring.Monitoring
import play.api.libs.json.Format
import serialization.{decode, decode2}

import scala.util.{Failure, Success}

abstract class ActorTransaction[ExternalDto](
    monitoring: Monitoring
)(implicit executionContext: ExecutionContext, format: Format[ExternalDto], c: ClassTag[ExternalDto])
    extends ActorTransactionMetrics(monitoring)(executionContext) {

  val topic: String
  def processCommand(registro: ExternalDto): Future[Response.SuccessProcessing]

  final def transaction(input: String): Future[Response.SuccessProcessing] = {
    recordRequests()
    processInput(input) match {
      case Left(serializationError) =>
        recordErrors(serializationError)
        Future.failed(serializationError)

      case Right(value) =>
        val future = processCommand(value)
        future.onComplete {
          case Failure(exception) =>
            recordErrors(exception)
          case Success(value) =>
            value
        }
        recordLatency(future)
        future
    }

  }

  def processInput(input: String): Either[Throwable, ExternalDto] =
    decode2[ExternalDto](input) // ktoso: Blocking in a future blocks the server

  final def route(implicit system: akka.actor.ActorSystem, requirements: KafkaMessageProcessorRequirements): Route =
    new ActorTransactionController(this, requirements)(system).route

  final def startAnyways()(implicit system: akka.actor.ActorSystem, requirements: KafkaMessageProcessorRequirements) = {
    Cluster(system).registerOnMemberUp { () =>
      (1 to 10) foreach { _ =>
        println(s"HEREEE! MEMBER UP! STARTING TRANSACTION for ${topic}!")
      }
      val rebalancerListener =
        system.actorOf(Props(
                         new TopicListener(
                           typeKeyName = "rebalancerListener",
                           monitoring
                         )
                       ),
                       name = "rebalancerListener")
      new KafkaTransactionalMessageProcessor(requirements.copy(rebalancerListener = rebalancerListener))
        .run(topic, s"${topic}SINK", message => {
          transaction(message).map { output =>
            Seq(output.toString)
          }
        })
    }

  }
}
