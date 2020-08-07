package consumers.registral.actividad_sujeto.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoCommands.ActividadSujetoUpdateFromDto
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import consumers.registral.actividad_sujeto.domain.ActividadSujetoState
import design_principles.actor_model.Response

class ActividadSujetoUpdateFromDtoHandler() {

  def handle(command: ActividadSujetoUpdateFromDto)(replyTo: ActorRef[Success]) = {
    Effect
      .persist[
        ActividadSujetoUpdatedFromDto,
        ActividadSujetoState
      ](
        ActividadSujetoUpdatedFromDto(
          command.sujetoId,
          command.actividadSujetoId,
          command.registro
        )
      )
      .thenReply(replyTo)(state => Success(Response.SuccessProcessing(command.deliveryId)))
  }

}
