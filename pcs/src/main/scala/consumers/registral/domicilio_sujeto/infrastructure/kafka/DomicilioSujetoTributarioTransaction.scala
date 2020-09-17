package consumers.registral.domicilio_sujeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoCommands
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoExternalDto.{
  DomicilioSujetoAnt,
  DomicilioSujetoTri
}
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class DomicilioSujetoTributarioTransaction(actor: DomicilioSujetoActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[DomicilioSujetoTri](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-DOMICILIO-SUJ-TRI"

  def processInput(input: String): Either[Throwable, DomicilioSujetoTri] =
    maybeDecode[DomicilioSujetoTri](input)

  override def processMessage(registro: DomicilioSujetoTri): Future[Response.SuccessProcessing] = {
    val command = DomicilioSujetoCommands.DomicilioSujetoUpdateFromDto(
      sujetoId = registro.BDS_SUJ_IDENTIFICADOR,
      domicilioId = registro.BDS_DOM_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor ask command
  }
}
