package consumers.registral.declaracion_jurada.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.calendario.domain.CalendarioEvents.CalendarioUpdatedFromDto
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaCommands.DeclaracionJuradaUpdateFromDto
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents.DeclaracionJuradaUpdatedFromDto
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaState
import design_principles.actor_model.Response
import kafka.MessageProducer
import consumers.registral.declaracion_jurada.infrastructure.json._
import kafka.KafkaMessageProducer.KafkaKeyValue
class DeclaracionJuradaUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: DeclaracionJuradaUpdateFromDto)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        DeclaracionJuradaUpdatedFromDto,
        DeclaracionJuradaState
      ](
        DeclaracionJuradaUpdatedFromDto(
          command.deliveryId,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.declaracionJuradaId,
          command.registro
        )
      )
      .thenReply(replyTo) { state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(
              command.aggregateRoot,
              serialization.encode(
                DeclaracionJuradaUpdatedFromDto(
                  command.deliveryId,
                  command.sujetoId,
                  command.objetoId,
                  command.tipoObjeto,
                  command.declaracionJuradaId,
                  command.registro
                )
              )
            )
          ),
          "DeclaracionJuradaUpdatedFromDto"
        )(_ => ())
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }

}
