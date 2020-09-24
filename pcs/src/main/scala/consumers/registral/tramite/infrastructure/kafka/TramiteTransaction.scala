package consumers.registral.tramite.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.subasta.application.entities.SubastaExternalDto
import consumers.registral.tramite.application.entities.TramiteExternalDto.Tramite
import consumers.registral.tramite.application.entities.{TramiteCommands, TramiteExternalDto}
import consumers.registral.tramite.infrastructure.dependency_injection.TramiteActor
import consumers.registral.tramite.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class TramiteTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[Tramite](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-TRAMITES"

  def processInput(input: String): Either[Throwable, Tramite] =
    maybeDecode[Tramite](input)

  override def processMessage(registro: Tramite): Future[Response.SuccessProcessing] = {
    val command = TramiteCommands.TramiteUpdateFromDto(
      sujetoId = registro.BTR_SUJ_IDENTIFICADOR,
      tramiteId = registro.BTR_TRMID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )

    actor ! command
    Future.successful(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }

}
