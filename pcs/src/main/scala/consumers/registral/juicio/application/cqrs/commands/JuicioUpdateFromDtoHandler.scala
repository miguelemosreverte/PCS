package consumers.registral.juicio.application.cqrs.commands

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.juicio.application.entities.JuicioCommands.JuicioUpdateFromDto
import consumers.registral.juicio.domain.JuicioEvents.JuicioUpdatedFromDto
import consumers.registral.juicio.domain.JuicioState

class JuicioUpdateFromDtoHandler() {

  def handle(command: JuicioUpdateFromDto)(replyTo: ActorRef[akka.Done]) =
    Effect
      .persist[
        JuicioUpdatedFromDto,
        JuicioState
      ](
        JuicioUpdatedFromDto(
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.juicioId,
          command.registro,
          command.detallesJuicio
        )
      )
      .thenReply(replyTo)(state => akka.Done)

}
