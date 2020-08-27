package consumers.registral.parametrica_recargo.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto.{
  ParametricaRecargoAnt,
  ParametricaRecargoTri
}
import consumers.registral.parametrica_recargo.application.entities.{
  ParametricaRecargoCommands,
  ParametricaRecargoExternalDto
}
import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decode2, decodeF}

import scala.concurrent.{ExecutionContext, Future}

case class ParametricaRecargoTributarioTransaction(actor: ParametricaRecargoActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ParametricaRecargoTri](monitoring) {

  val topic = "DGR-COP-PARAMRECARGO-TRI"

  def processInput(input: String): Either[Throwable, ParametricaRecargoTri] =
    decode2[ParametricaRecargoTri](input)

  override def processCommand(registro: ParametricaRecargoTri): Future[Response.SuccessProcessing] = {
    val command = ParametricaRecargoCommands.ParametricaRecargoUpdateFromDto(
      parametricaRecargoId = registro.BPR_INDICE,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor ask command
  }
}
