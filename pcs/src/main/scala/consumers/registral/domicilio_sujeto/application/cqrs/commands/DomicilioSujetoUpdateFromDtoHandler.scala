package consumers.registral.domicilio_sujeto.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoCommands.DomicilioSujetoUpdateFromDto
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoState
import design_principles.actor_model.Response

class DomicilioSujetoUpdateFromDtoHandler() {

  def handle(command: DomicilioSujetoUpdateFromDto)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        DomicilioSujetoUpdatedFromDto,
        DomicilioSujetoState
      ](
        DomicilioSujetoUpdatedFromDto(
          command.deliveryId,
          command.sujetoId,
          command.domicilioId,
          command.registro
        )
      )
      .thenReply(replyTo)(state => Success(Response.SuccessProcessing(command.deliveryId)))

}
