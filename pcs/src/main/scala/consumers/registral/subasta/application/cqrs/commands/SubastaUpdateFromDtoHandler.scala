package consumers.registral.subasta.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.subasta.application.entities.SubastaCommands.SubastaUpdateFromDto
import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import consumers.registral.subasta.domain.SubastaState
import design_principles.actor_model.Response
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import consumers.registral.subasta.infrastructure.json._

class SubastaUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: SubastaUpdateFromDto)(replyTo: ActorRef[Success]) = {
    val event = SubastaUpdatedFromDto(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.subastaId,
      command.registro
    )
    Effect
      .persist[
        SubastaUpdatedFromDto,
        SubastaState
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
                                "SubastaUpdatedFromDto")(_ => ())
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
  }
}
