package consumers.registral.etapas_procesales.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesCommands.EtapasProcesalesUpdateFromDto
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents.EtapasProcesalesUpdatedFromDto
import consumers.registral.etapas_procesales.domain.EtapasProcesalesState
import design_principles.actor_model.Response
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import consumers.registral.etapas_procesales.infrastructure.json._

class EtapasProcesalesUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: EtapasProcesalesUpdateFromDto)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        EtapasProcesalesUpdatedFromDto,
        EtapasProcesalesState
      ](
        EtapasProcesalesUpdatedFromDto(
          command.deliveryId,
          command.juicioId,
          command.etapaId,
          command.registro
        )
      )
      .thenReply(replyTo) { state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(command.aggregateRoot,
                          serialization.encode(
                            EtapasProcesalesUpdatedFromDto(
                              command.deliveryId,
                              command.juicioId,
                              command.etapaId,
                              command.registro
                            )
                          ))
          ),
          "EtapasProcesalesUpdatedFromDto"
        )(_ => ())
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
}
