package api.actor_transaction

import akka.Done
import akka.actor.ActorRef
import monitoring.Monitoring
import play.api.libs.json.Format
import serialization.decodeF

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class ActorTransaction[ExternalDto](
    monitoring: Monitoring
)(implicit ec: ExecutionContext, format: Format[ExternalDto], c: ClassTag[ExternalDto])
    extends ActorTransactionMetrics(monitoring)
    with ActorTransactionRoutes {
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
