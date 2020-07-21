package consumers.registral.tramite.application.cqrs.commands

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.tramite.application.entities.TramiteCommands.TramiteUpdateFromDto
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import consumers.registral.tramite.domain.TramiteState

class TramiteUpdateFromDtoHandler() {

  def handle(command: TramiteUpdateFromDto)(replyTo: ActorRef[akka.Done]) =
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
      .thenReply(replyTo)(state => akka.Done)

}
