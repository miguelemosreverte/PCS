package consumers.registral.parametrica_recargo.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto.{
  ParametricaRecargoAnt,
  ParametricaRecargoTri
}
import consumers.registral.parametrica_recargo.application.entities.{
  ParametricaRecargoCommands,
  ParametricaRecargoExternalDto
}
import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class ParametricaRecargoTributarioTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ParametricaRecargoTri](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-PARAMRECARGO-TRI"

  def processInput(input: String): Either[Throwable, ParametricaRecargoTri] =
    maybeDecode[ParametricaRecargoTri](input)

  override def processMessage(registro: ParametricaRecargoTri): Future[Response.SuccessProcessing] = {
    val command = ParametricaRecargoCommands.ParametricaRecargoUpdateFromDto(
      parametricaRecargoId = registro.BPR_INDICE,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor ask command
  }
}
