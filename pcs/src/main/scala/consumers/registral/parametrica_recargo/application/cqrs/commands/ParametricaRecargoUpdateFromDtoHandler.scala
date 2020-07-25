package consumers.registral.parametrica_recargo.application.cqrs.commands

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoCommands.ParametricaRecargoUpdateFromDto
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto
import consumers.registral.parametrica_recargo.domain.{ParametricaRecargoEvents, ParametricaRecargoState}

class ParametricaRecargoUpdateFromDtoHandler() {

  def handle(command: ParametricaRecargoUpdateFromDto)(replyTo: ActorRef[akka.Done]) = {
    val registro = command.registro
    Effect
      .persist[
        ParametricaRecargoUpdatedFromDto,
        ParametricaRecargoState
      ](
        ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto(
          bprIndice = registro.BPR_INDICE,
          bprTipoIndice = registro.BPR_TIPO_INDICE,
          bprFechaDesde = registro.BPR_FECHA_DESDE,
          bprPeriodo = registro.BPR_PERIODO,
          bprConcepto = registro.BPR_CONCEPTO,
          bprImpuesto = registro.BPR_IMPUESTO,
          registro
        )
      )
      .thenReply(replyTo)(state => akka.Done)
  }

}
