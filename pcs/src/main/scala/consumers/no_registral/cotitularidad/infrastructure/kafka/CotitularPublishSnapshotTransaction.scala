package consumers.no_registral.cotitularidad.infrastructure.kafka

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.CotitularidadPublishSnapshot
import consumers.no_registral.cotitularidad.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaClassicTypedAsk
import monitoring.Monitoring
import play.api.libs.json.Json

case class CotitularPublishSnapshotTransaction(monitoring: Monitoring)(implicit actorRef: ActorRef,
                                                                       ec: ExecutionContext)
    extends ActorTransaction(monitoring) {

  val topic = "CotitularidadPublishSnapshot"

  override def transaction(input: String): Future[Done] =
    for {
      cmd <- processInput(input)
      done <- processCommand(cmd)
    } yield done

  def processInput(input: String): Future[CotitularidadPublishSnapshot] = Future {
    Json.parse(input).as[CotitularidadPublishSnapshot]
  }

  def processCommand(cmd: CotitularidadPublishSnapshot): Future[Done] = {
    actorRef.ask[akka.Done](cmd)
  }
}
