package api.actor_transaction

import java.time.LocalDateTime

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import akka.http.scaladsl.server.Route
import design_principles.actor_model.Response
import design_principles.threading.bulkhead_pattern.bulkheads.ActorBulkhead
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring
import play.api.libs.json.Format
import serialization.{decode, decode2}

import scala.util.{Failure, Success}

abstract class ActorTransaction[ExternalDto](
    monitoring: Monitoring
)(implicit format: Format[ExternalDto], c: ClassTag[ExternalDto])
    extends ActorTransactionMetrics(monitoring) {

  val topic: String
  final implicit val executionContext = ActorBulkhead.executionContext

  def processCommand(registro: ExternalDto): Future[Response.SuccessProcessing]

  final def transaction(input: String): Future[Response.SuccessProcessing] = {
    recordRequests()
    val now = LocalDateTime.now().getNano
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
            val after = LocalDateTime.now().getNano
            recordLatency(after - now)
            value
        }
        future
    }

  }

  def processInput(input: String): Either[Throwable, ExternalDto] =
    decode2[ExternalDto](input) // ktoso: Blocking in a future blocks the server

  final def route(implicit system: akka.actor.ActorSystem, requirements: KafkaMessageProcessorRequirements): Route =
    new ActorTransactionController(this, requirements)(system).route
}
