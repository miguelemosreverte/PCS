package consumers.registral.domicilio_sujeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoCommands
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoExternalDto.DomicilioSujetoTri
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.{ExecutionContext, Future}

case class DomicilioSujetoTributarioTransaction(actor: DomicilioSujetoActor, monitoring: Monitoring)(
    implicit
    system: ActorSystem[_],
    ec: ExecutionContext
) extends ActorTransaction[DomicilioSujetoTri](monitoring) {

  val topic = "DGR-COP-DOMICILIO-SUJ-TRI"

  override def processCommand(registro: DomicilioSujetoTri): Future[Done] = {
    val command = DomicilioSujetoCommands.DomicilioSujetoUpdateFromDto(
      sujetoId = registro.BDS_SUJ_IDENTIFICADOR,
      domicilioId = registro.BDS_DOM_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor ask command
  }
}
