package consumers.registral.juicio.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.juicio.application.entities.JuicioCommands.JuicioUpdateFromDto
import consumers.registral.juicio.domain.JuicioEvents.JuicioUpdatedFromDto
import consumers.registral.juicio.domain.JuicioState
import design_principles.actor_model.Response

class JuicioUpdateFromDtoHandler() {

  def handle(command: JuicioUpdateFromDto)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        JuicioUpdatedFromDto,
        JuicioState
      ](
        JuicioUpdatedFromDto(
          command.deliveryId,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.juicioId,
          command.registro,
          command.detallesJuicio
        )
      )
      .thenReply(replyTo)(state => Success(Response.SuccessProcessing(command.deliveryId)))

}
