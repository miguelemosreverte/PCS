package consumers.registral.parametrica_recargo.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoCommands.ParametricaRecargoUpdateFromDto
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto
import consumers.registral.parametrica_recargo.domain.{ParametricaRecargoEvents, ParametricaRecargoState}
import design_principles.actor_model.Response
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import consumers.registral.parametrica_recargo.infrastructure.json._

class ParametricaRecargoUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: ParametricaRecargoUpdateFromDto)(replyTo: ActorRef[Success]) = {

    val registro = command.registro
    val event = ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto(
      command.deliveryId,
      bprIndice = registro.BPR_INDICE,
      bprTipoIndice = registro.BPR_TIPO_INDICE,
      bprFechaDesde = registro.BPR_FECHA_DESDE,
      bprPeriodo = registro.BPR_PERIODO,
      bprConcepto = registro.BPR_CONCEPTO,
      bprImpuesto = registro.BPR_IMPUESTO,
      registro
    )
    Effect
      .persist[
        ParametricaRecargoUpdatedFromDto,
        ParametricaRecargoState
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
                                "ParametricaRecargoUpdatedFromDto")(_ => ())
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
  }

}
