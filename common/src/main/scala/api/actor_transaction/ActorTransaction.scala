package api.actor_transaction

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import design_principles.actor_model.Response
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring
import play.api.libs.json.Format
import serialization.decode2

import scala.util.{Failure, Success, Try}

abstract class ActorTransaction[ExternalDto](
    monitoring: Monitoring
)(implicit actorTransactionRequirements: ActorTransactionRequirements)
    extends ActorTransactionMetrics(monitoring)(actorTransactionRequirements.executionContext) {

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
          case Failure(exception) => recordErrors(exception)
          case Success(_) => ()
        }(actorTransactionRequirements.executionContext)
        recordLatency(future)
        future
    }
  }

  def processInput(input: String): Either[Throwable, ExternalDto]

  final def route(implicit system: akka.actor.ActorSystem, requirements: KafkaMessageProcessorRequirements): Route =
    new ActorTransactionController(this, requirements)(system).route
}

object ActorTransaction {
  case class ActorTransactionRequirements(executionContext: ExecutionContext)
}
