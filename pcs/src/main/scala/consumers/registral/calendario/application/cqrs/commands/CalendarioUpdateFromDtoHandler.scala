package consumers.registral.calendario.application.cqrs.commands

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.calendario.application.entities.CalendarioCommands.CalendarioUpdateFromDto
import consumers.registral.calendario.domain.CalendarioEvents.CalendarioUpdatedFromDto
import consumers.registral.calendario.domain.CalendarioState

class CalendarioUpdateFromDtoHandler() {

  def handle(command: CalendarioUpdateFromDto)(replyTo: ActorRef[akka.Done]) =
    Effect
      .persist[
        CalendarioUpdatedFromDto,
        CalendarioState
      ](
        CalendarioUpdatedFromDto(
          command.aggregateRoot,
          command.registro
        )
      )
      .thenReply(replyTo)(state => akka.Done)

}
