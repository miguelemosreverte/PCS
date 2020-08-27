package consumers.registral.parametrica_plan.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanExternalDto.{
  ParametricaPlanAnt,
  ParametricaPlanTri
}
import consumers.registral.parametrica_plan.application.entities.{ParametricaPlanCommands, ParametricaPlanExternalDto}
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decode2, decodeF}

import scala.concurrent.{ExecutionContext, Future}

case class ParametricaPlanTributarioTransaction(actor: ParametricaPlanActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ParametricaPlanTri](monitoring) {

  val topic = "DGR-COP-PARAMPLAN-TRI"

  def processInput(input: String): Either[Throwable, ParametricaPlanTri] =
    decode2[ParametricaPlanTri](input)

  override def processCommand(registro: ParametricaPlanTri): Future[Response.SuccessProcessing] = {

    val command = ParametricaPlanCommands.ParametricaPlanUpdateFromDto(
      parametricaPlanId = registro.BPP_FPM_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor ask command
  }

}
