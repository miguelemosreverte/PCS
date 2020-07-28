package consumers.registral.parametrica_plan.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanExternalDto.ParametricaPlanAnt
import consumers.registral.parametrica_plan.application.entities.{ParametricaPlanCommands, ParametricaPlanExternalDto}
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.{ExecutionContext, Future}

case class ParametricaPlanNoTributarioTransaction()(implicit actor: ParametricaPlanActor,
                                                    system: akka.actor.typed.ActorSystem[_],
                                                    monitoring: Monitoring,
                                                    ec: ExecutionContext)
    extends ActorTransaction[ParametricaPlanAnt](monitoring) {

  val topic = "DGR-COP-PARAMPLAN-ANT"

  override def processCommand(registro: ParametricaPlanAnt): Future[Done] = {
    val command = registro match {
      case registro: ParametricaPlanExternalDto.ParametricaPlanAnt =>
        ParametricaPlanCommands.ParametricaPlanUpdateFromDto(
          parametricaPlanId = registro.BPP_FPM_ID,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }

}
