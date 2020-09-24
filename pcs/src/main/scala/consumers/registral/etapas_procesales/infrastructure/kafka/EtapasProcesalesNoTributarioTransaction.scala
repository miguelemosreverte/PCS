package consumers.registral.etapas_procesales.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoExternalDto.DomicilioSujetoTri
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesExternalDto.EtapasProcesalesAnt
import consumers.registral.etapas_procesales.application.entities.{
  EtapasProcesalesCommands,
  EtapasProcesalesExternalDto
}
import consumers.registral.etapas_procesales.infrastructure.dependency_injection.EtapasProcesalesActor
import consumers.registral.etapas_procesales.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class EtapasProcesalesNoTributarioTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[EtapasProcesalesAnt](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-ETAPROCESALES-ANT"

  def processInput(input: String): Either[Throwable, EtapasProcesalesAnt] =
    maybeDecode[EtapasProcesalesAnt](input)

  override def processMessage(registro: EtapasProcesalesAnt): Future[Response.SuccessProcessing] = {
    val command = EtapasProcesalesCommands.EtapasProcesalesUpdateFromDto(
      juicioId = registro.BEP_JUI_ID,
      etapaId = registro.BPE_ETA_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )

    actor ! command
    Future.successful(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }

}
