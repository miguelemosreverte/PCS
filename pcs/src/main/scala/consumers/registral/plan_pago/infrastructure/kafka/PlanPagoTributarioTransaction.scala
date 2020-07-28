package consumers.registral.plan_pago.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import consumers.registral.plan_pago.application.entities.PlanPagoExternalDto.PlanPagoTri
import consumers.registral.plan_pago.application.entities.{PlanPagoCommands, PlanPagoExternalDto}
import consumers.registral.plan_pago.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.Future

case class PlanPagoTributarioTransaction(monitoring: Monitoring)(implicit actor: PlanPagoActor,
                                                                 system: akka.actor.typed.ActorSystem[_])
    extends ActorTransaction(monitoring) {

  val topic = "DGR-COP-PLANES-TRI"

  override def transaction(input: String): Future[Done] = {
    val registro: PlanPagoTri = decodeF[PlanPagoTri](input)
    val command = registro match {
      case registro: PlanPagoExternalDto.PlanPagoTri =>
        PlanPagoCommands.PlanPagoUpdateFromDto(
          sujetoId = registro.BPL_SUJ_IDENTIFICADOR,
          objetoId = registro.BPL_SOJ_IDENTIFICADOR,
          tipoObjeto = registro.BPL_SOJ_TIPO_OBJETO,
          planPagoId = registro.BPL_PLN_ID,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }
}
