package consumers.registral.tramite.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.tramite.application.entities.TramiteCommands.TramiteUpdateFromDto
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import consumers.registral.tramite.domain.TramiteState
import design_principles.actor_model.Response
import kafka.KafkaMessageProducer.KafkaKeyValue
import consumers.registral.tramite.infrastructure.json._
import kafka.MessageProducer

class TramiteUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: TramiteUpdateFromDto)(replyTo: ActorRef[Success]) = {
    val event = TramiteUpdatedFromDto(
      command.deliveryId,
      command.sujetoId,
      command.tramiteId,
      command.registro
    )
    Effect
      .persist[
        TramiteUpdatedFromDto,
        TramiteState
      ](
        event
      )
      .thenReply(replyTo) { state =>
        messageProducer.produce(Seq(
                                  KafkaKeyValue(command.aggregateRoot,
                                                serialization.encode(
                                                  event
                                                ))
                                ),
                                "TramiteUpdatedFromDto")(_ => ())
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
  }
}
