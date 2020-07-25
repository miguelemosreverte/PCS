package consumers.registral.domicilio_objeto.application.cqrs.commands

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoCommands.DomicilioObjetoUpdateFromDto
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents.DomicilioObjetoUpdatedFromDto
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoState

class DomicilioObjetoUpdateFromDtoHandler() {

  def handle(command: DomicilioObjetoUpdateFromDto)(replyTo: ActorRef[akka.Done]) =
    Effect
      .persist[
        DomicilioObjetoUpdatedFromDto,
        DomicilioObjetoState
      ](
        DomicilioObjetoUpdatedFromDto(
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.domicilioId,
          command.registro
        )
      )
      .thenReply(replyTo)(state => akka.Done)

}
