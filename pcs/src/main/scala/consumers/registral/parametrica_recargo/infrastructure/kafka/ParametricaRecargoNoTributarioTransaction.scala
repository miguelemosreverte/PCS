package consumers.registral.parametrica_recargo.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto.ParametricaRecargoAnt
import consumers.registral.parametrica_recargo.application.entities.{
  ParametricaRecargoCommands,
  ParametricaRecargoExternalDto
}
import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.{ExecutionContext, Future}

case class ParametricaRecargoNoTributarioTransaction(actor: ParametricaRecargoActor, monitoring: Monitoring)(
    implicit
    system: akka.actor.typed.ActorSystem[_],
    ec: ExecutionContext
) extends ActorTransaction[ParametricaRecargoAnt](monitoring) {

  val topic = "DGR-COP-PARAMRECARGO-ANT"

  override def processCommand(registro: ParametricaRecargoAnt): Future[Response.SuccessProcessing] = {

    val command = ParametricaRecargoCommands.ParametricaRecargoUpdateFromDto(
      parametricaRecargoId = registro.BPR_INDICE,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor ask command
  }

}
