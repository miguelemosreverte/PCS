package consumers.registral.etapas_procesales.application.cqrs.commands

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesCommands.EtapasProcesalesUpdateFromDto
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents.EtapasProcesalesUpdatedFromDto
import consumers.registral.etapas_procesales.domain.EtapasProcesalesState

class EtapasProcesalesUpdateFromDtoHandler() {

  def handle(command: EtapasProcesalesUpdateFromDto)(replyTo: ActorRef[akka.Done]) =
    Effect
      .persist[
        EtapasProcesalesUpdatedFromDto,
        EtapasProcesalesState
      ](
        EtapasProcesalesUpdatedFromDto(
          command.juicioId,
          command.etapaId,
          command.registro
        )
      )
      .thenReply(replyTo)(state => akka.Done)

}
