package consumers.registral.subasta.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.subasta.application.entities.SubastaCommands.SubastaUpdateFromDto
import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import consumers.registral.subasta.domain.SubastaState
import design_principles.actor_model.Response

class SubastaUpdateFromDtoHandler() {

  def handle(command: SubastaUpdateFromDto)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        SubastaUpdatedFromDto,
        SubastaState
      ](
        SubastaUpdatedFromDto(
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.subastaId,
          command.registro
        )
      )
      .thenReply(replyTo)(state => Success(Response.SuccessProcessing(command.deliveryId)))

}
