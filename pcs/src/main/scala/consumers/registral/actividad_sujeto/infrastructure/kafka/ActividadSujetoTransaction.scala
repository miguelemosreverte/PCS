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

import scala.concurrent.Future

case class ActividadSujetoTransaction(monitoring: Monitoring)(implicit actor: ActividadSujetoActor,
                                                              system: ActorSystem[_])
    extends ActorTransaction(monitoring) {

  val topic = "DGR-COP-ACTIVIDADES"

  override def transaction(input: String): Future[Done] = {
    import scala.concurrent.duration._
    implicit val timeout: Timeout = Timeout(30 seconds)
    val registro: ActividadSujeto = decodeF[ActividadSujeto](input)

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