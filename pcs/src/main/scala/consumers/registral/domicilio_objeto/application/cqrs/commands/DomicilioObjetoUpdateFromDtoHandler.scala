package consumers.registral.domicilio_objeto.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents.DeclaracionJuradaUpdatedFromDto
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoCommands.DomicilioObjetoUpdateFromDto
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents.DomicilioObjetoUpdatedFromDto
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoState
import design_principles.actor_model.Response
import kafka.MessageProducer
import consumers.registral.domicilio_objeto.infrastructure.json._
import kafka.KafkaMessageProducer.KafkaKeyValue

class DomicilioObjetoUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: DomicilioObjetoUpdateFromDto)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        DomicilioObjetoUpdatedFromDto,
        DomicilioObjetoState
      ](
        DomicilioObjetoUpdatedFromDto(
          command.deliveryId,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.domicilioId,
          command.registro
        )
      )
      .thenReply(replyTo) { state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(
              command.aggregateRoot,
              serialization.encode(
                DomicilioObjetoUpdatedFromDto(
                  command.deliveryId,
                  command.sujetoId,
                  command.objetoId,
                  command.tipoObjeto,
                  command.domicilioId,
                  command.registro
                )
              )
            )
          ),
          "DomicilioObjetoUpdatedFromDto"
        )(_ => ())
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }

}
