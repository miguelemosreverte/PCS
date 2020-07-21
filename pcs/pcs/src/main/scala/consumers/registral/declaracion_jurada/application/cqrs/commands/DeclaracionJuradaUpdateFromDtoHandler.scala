package consumers.registral.declaracion_jurada.application.cqrs.commands

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaCommands.DeclaracionJuradaUpdateFromDto
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents.DeclaracionJuradaUpdatedFromDto
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaState

class DeclaracionJuradaUpdateFromDtoHandler() {

  def handle(command: DeclaracionJuradaUpdateFromDto)(replyTo: ActorRef[akka.Done]) =
    Effect
      .persist[
        DeclaracionJuradaUpdatedFromDto,
        DeclaracionJuradaState
      ](
        DeclaracionJuradaUpdatedFromDto(
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.declaracionJuradaId,
          command.registro
        )
      )
      .thenReply(replyTo)(state => akka.Done)

}
