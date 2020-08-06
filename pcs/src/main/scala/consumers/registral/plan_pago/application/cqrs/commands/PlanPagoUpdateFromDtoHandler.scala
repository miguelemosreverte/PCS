package consumers.registral.plan_pago.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.plan_pago.application.entities.PlanPagoCommands.PlanPagoUpdateFromDto
import consumers.registral.plan_pago.domain.PlanPagoEvents.PlanPagoUpdatedFromDto
import consumers.registral.plan_pago.domain.PlanPagoState
import design_principles.actor_model.Response

class PlanPagoUpdateFromDtoHandler() {

  def handle(command: PlanPagoUpdateFromDto)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        PlanPagoUpdatedFromDto,
        PlanPagoState
      ](
        PlanPagoUpdatedFromDto(
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.planPagoId,
          command.registro
        )
      )
      .thenReply(replyTo)(state => Success(Response.SuccessProcessing()))

}
