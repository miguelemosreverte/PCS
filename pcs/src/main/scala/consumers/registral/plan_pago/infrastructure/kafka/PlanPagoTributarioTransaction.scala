package consumers.registral.plan_pago.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import consumers.registral.plan_pago.application.entities.PlanPagoExternalDto.PlanPagoTri
import consumers.registral.plan_pago.application.entities.{PlanPagoCommands, PlanPagoExternalDto}
import consumers.registral.plan_pago.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.{ExecutionContext, Future}

case class PlanPagoTributarioTransaction(actor: PlanPagoActor, monitoring: Monitoring)(
    implicit
    system: akka.actor.typed.ActorSystem[_],
    ec: ExecutionContext
) extends ActorTransaction[PlanPagoTri](monitoring) {

  val topic = "DGR-COP-PLANES-TRI"

  override def processCommand(registro: PlanPagoTri): Future[Response.SuccessProcessing] = {
    val command = PlanPagoCommands.PlanPagoUpdateFromDto(
      sujetoId = registro.BPL_SUJ_IDENTIFICADOR,
      objetoId = registro.BPL_SOJ_IDENTIFICADOR,
      tipoObjeto = registro.BPL_SOJ_TIPO_OBJETO,
      planPagoId = registro.BPL_PLN_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor ask command
  }
}
