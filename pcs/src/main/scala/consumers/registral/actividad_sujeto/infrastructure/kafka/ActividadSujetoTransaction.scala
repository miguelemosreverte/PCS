package consumers.registral.actividad_sujeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import akka.util.Timeout
import api.actor_transaction.ActorTransaction
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoCommands.ActividadSujetoUpdateFromDto
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoExternalDto.ActividadSujeto
import consumers.registral.actividad_sujeto.infrastructure.dependency_injection.ActividadSujetoActor
import consumers.registral.actividad_sujeto.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.{ExecutionContext, Future}

case class ActividadSujetoTransaction(actor: ActividadSujetoActor, monitoring: Monitoring)(implicit
                                                                                           system: ActorSystem[_],
                                                                                           ec: ExecutionContext)
    extends ActorTransaction[ActividadSujeto](monitoring) {

  val topic = "DGR-COP-ACTIVIDADES"

  override def processCommand(registro: ActividadSujeto): Future[Done] = {
    val command =
      ActividadSujetoUpdateFromDto(
        sujetoId = registro.BAT_SUJ_IDENTIFICADOR,
        actividadSujetoId = registro.BAT_ATD_ID,
        deliveryId = BigInt(registro.EV_ID),
        registro = registro
      )

    actor ask command
  }
}
