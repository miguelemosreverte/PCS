package consumers.registral.tramite.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.tramite.application.entities.TramiteCommands.TramiteUpdateFromDto
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import consumers.registral.tramite.domain.TramiteState
import design_principles.actor_model.Response

class TramiteUpdateFromDtoHandler() {

  def handle(command: TramiteUpdateFromDto)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        TramiteUpdatedFromDto,
        TramiteState
      ](
        TramiteUpdatedFromDto(
          command.sujetoId,
          command.tramiteId,
          command.registro
        )
      )
      .thenReply(replyTo)(state => Success(Response.SuccessProcessing(command.deliveryId)))

}
