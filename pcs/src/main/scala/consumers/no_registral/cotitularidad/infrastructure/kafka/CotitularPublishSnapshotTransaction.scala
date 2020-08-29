package consumers.no_registral.cotitularidad.infrastructure.kafka

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.{
  CotitularidadAddSujetoCotitular,
  CotitularidadPublishSnapshot
}
import consumers.no_registral.cotitularidad.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaClassicTypedAsk
import monitoring.Monitoring
import play.api.libs.json.Json
import serialization.decode2

case class CotitularPublishSnapshotTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[CotitularidadPublishSnapshot](monitoring) {

  def topic = "CotitularidadPublishSnapshot"

  def processInput(input: String): Either[Throwable, CotitularidadPublishSnapshot] =
    decode2[CotitularidadPublishSnapshot](input)

  def processCommand(cmd: CotitularidadPublishSnapshot): Future[Response.SuccessProcessing] = {
    actorRef.ask[Response.SuccessProcessing](cmd)
  }
}
