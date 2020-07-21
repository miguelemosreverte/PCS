package consumers.registral.domicilio_sujeto.application.cqrs.commands

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoCommands.DomicilioSujetoUpdateFromDto
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoState

class DomicilioSujetoUpdateFromDtoHandler() {

  def handle(command: DomicilioSujetoUpdateFromDto)(replyTo: ActorRef[akka.Done]) =
    Effect
      .persist[
        DomicilioSujetoUpdatedFromDto,
        DomicilioSujetoState
      ](
        DomicilioSujetoUpdatedFromDto(
          command.sujetoId,
          command.domicilioId,
          command.registro
        )
      )
      .thenReply(replyTo)(state => akka.Done)

}
