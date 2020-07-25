package consumers.registral.parametrica_recargo.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto.ParametricaRecargoTri
import consumers.registral.parametrica_recargo.application.entities.{
  ParametricaRecargoCommands,
  ParametricaRecargoExternalDto
}
import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import serialization.decodeF

import scala.concurrent.Future

case class ParametricaRecargoTributarioTransaction()(implicit actor: ParametricaRecargoActor,
                                                     system: akka.actor.typed.ActorSystem[_])
    extends ActorTransaction {

  val topic = "DGR-COP-PARAMRECARGO-TRI"

  override def transaction(input: String): Future[Done] = {

    val registro: ParametricaRecargoTri = decodeF[ParametricaRecargoTri](input)
    val command = registro match {
      case registro: ParametricaRecargoExternalDto.ParametricaRecargoTri =>
        ParametricaRecargoCommands.ParametricaRecargoUpdateFromDto(
          parametricaRecargoId = registro.BPR_INDICE,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }
}
