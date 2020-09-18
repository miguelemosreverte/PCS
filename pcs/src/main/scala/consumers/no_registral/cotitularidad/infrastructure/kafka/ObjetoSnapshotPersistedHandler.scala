package consumers.no_registral.cotitularidad.infrastructure.kafka

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.{
  CotitularidadPublishSnapshot,
  ObjetoSnapshotPersistedReaction
}
import consumers.no_registral.objeto.infrastructure.json._
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaClassicTypedAsk
import monitoring.Monitoring
import play.api.libs.json.Json
import serialization.maybeDecode

case class ObjetoSnapshotPersistedHandler(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObjetoSnapshotPersisted](monitoring) {

  def topic = "ObjetoSnapshotPersisted"

  def processInput(input: String): Either[Throwable, ObjetoSnapshotPersisted] =
    maybeDecode[ObjetoSnapshotPersisted](input)

  def processMessage(evt: ObjetoSnapshotPersisted): Future[Response.SuccessProcessing] = {
    actorRef.ask[Response.SuccessProcessing](
      ObjetoSnapshotPersistedReaction(
        evt.deliveryId,
        evt.objetoId,
        evt.tipoObjeto,
        evt
      )
    )
  }
}
