package api.actor_transaction

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

import akka.http.scaladsl.server.Route
import design_principles.actor_model.Response
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring
import play.api.libs.json.Format
import serialization.decodeF

abstract class ActorTransaction[ExternalDto](
    monitoring: Monitoring
)(implicit ec: ExecutionContext, format: Format[ExternalDto], c: ClassTag[ExternalDto])
    extends ActorTransactionMetrics(monitoring) {

  val topic: String

  def processCommand(registro: ExternalDto): Future[Response.SuccessProcessing]

  final def transaction(input: String): Future[Response.SuccessProcessing] = {
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

  final def route(implicit system: akka.actor.ActorSystem, requirements: KafkaMessageProcessorRequirements): Route =
    new ActorTransactionController(this, requirements)(system).route
}
