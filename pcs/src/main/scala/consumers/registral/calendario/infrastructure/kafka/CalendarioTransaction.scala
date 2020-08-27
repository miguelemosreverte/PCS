package consumers.registral.calendario.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoExternalDto.ActividadSujeto
import consumers.registral.calendario.application.entities.{CalendarioCommands, CalendarioExternalDto}
import consumers.registral.calendario.infrastructure.dependency_injection.CalendarioActor
import consumers.registral.calendario.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decode2, decodeF}

import scala.concurrent.{ExecutionContext, Future}

case class CalendarioTransaction(actor: CalendarioActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[CalendarioExternalDto](monitoring) {

  val topic = "DGR-COP-CALENDARIO"

  def processInput(input: String): Either[Throwable, CalendarioExternalDto] =
    decode2[CalendarioExternalDto](input)

  override def processCommand(registro: CalendarioExternalDto): Future[Response.SuccessProcessing] = {
    val command = registro match {
      case registro: CalendarioExternalDto =>
        CalendarioCommands.CalendarioUpdateFromDto(
          calendarioId = registro.BCL_IDENTIFICADOR,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }

}
