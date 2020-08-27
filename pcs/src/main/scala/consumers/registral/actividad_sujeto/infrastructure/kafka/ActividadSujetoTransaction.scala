package consumers.registral.actividad_sujeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import akka.util.Timeout
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoTri
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoCommands.ActividadSujetoUpdateFromDto
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoExternalDto.ActividadSujeto
import consumers.registral.actividad_sujeto.infrastructure.dependency_injection.ActividadSujetoActor
import consumers.registral.actividad_sujeto.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decode2, decodeF}

import scala.concurrent.{ExecutionContext, Future}

case class ActividadSujetoTransaction(actor: ActividadSujetoActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ActividadSujeto](monitoring) {

  val topic = "DGR-COP-ACTIVIDADES"

  def processInput(input: String): Either[Throwable, ActividadSujeto] =
    decode2[ActividadSujeto](input)

  override def processCommand(registro: ActividadSujeto): Future[Response.SuccessProcessing] = {
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
