package consumers.registral.domicilio_sujeto.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoExternalDto.DomicilioObjetoTri
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoCommands
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoExternalDto.DomicilioSujetoAnt
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class DomicilioSujetoNoTributarioTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[DomicilioSujetoAnt](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-DOMICILIO-SUJ-ANT"

  def processInput(input: String): Either[Throwable, DomicilioSujetoAnt] =
    maybeDecode[DomicilioSujetoAnt](input)

  override def processMessage(registro: DomicilioSujetoAnt): Future[Response.SuccessProcessing] = {
    val command = DomicilioSujetoCommands.DomicilioSujetoUpdateFromDto(
      sujetoId = registro.BDS_SUJ_IDENTIFICADOR,
      domicilioId = registro.BDS_DOM_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )

    actor ask command
  }

}
