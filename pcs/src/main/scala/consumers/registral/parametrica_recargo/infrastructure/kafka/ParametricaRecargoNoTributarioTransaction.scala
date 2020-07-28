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
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.Future

case class ParametricaRecargoNoTributarioTransaction(monitoring: Monitoring)(implicit actor: ParametricaRecargoActor,
                                                                             system: akka.actor.typed.ActorSystem[_])
    extends ActorTransaction(monitoring) {

  val topic = "DGR-COP-PARAMRECARGO-ANT"

  override def transaction(input: String): Future[Done] = {

    val registro: ParametricaRecargoAnt = decodeF[ParametricaRecargoAnt](input)
    val command = registro match {
      case registro: ParametricaRecargoExternalDto.ParametricaRecargoAnt =>
        ParametricaRecargoCommands.ParametricaRecargoUpdateFromDto(
          parametricaRecargoId = registro.BPR_INDICE,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }

}
