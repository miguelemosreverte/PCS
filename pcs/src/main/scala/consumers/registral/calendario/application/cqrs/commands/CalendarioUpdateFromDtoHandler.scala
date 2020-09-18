package consumers.registral.calendario.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import consumers.registral.calendario.application.entities.CalendarioCommands.CalendarioUpdateFromDto
import consumers.registral.calendario.domain.CalendarioEvents.CalendarioUpdatedFromDto
import consumers.registral.calendario.domain.CalendarioState
import design_principles.actor_model.Response
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import consumers.registral.calendario.infrastructure.json._

class CalendarioUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: CalendarioUpdateFromDto)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        CalendarioUpdatedFromDto,
        CalendarioState
      ](
        CalendarioUpdatedFromDto(
          command.deliveryId,
          command.aggregateRoot,
          command.registro
        )
      )
      .thenReply(replyTo) { state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(
              command.aggregateRoot,
              serialization.encode(
                CalendarioUpdatedFromDto(
                  command.deliveryId,
                  command.aggregateRoot,
                  command.registro
                )
              )
            )
          ),
          "CalendarioUpdatedFromDto"
        )(_ => ())
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }

}
