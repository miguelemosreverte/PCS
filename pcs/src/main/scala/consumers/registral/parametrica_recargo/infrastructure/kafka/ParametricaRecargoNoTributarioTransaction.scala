package consumers.registral.parametrica_recargo.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanExternalDto.ParametricaPlanTri
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto.ParametricaRecargoAnt
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

case class ParametricaRecargoNoTributarioTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ParametricaRecargoAnt](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-PARAMRECARGO-ANT"

  def processInput(input: String): Either[Throwable, ParametricaRecargoAnt] =
    maybeDecode[ParametricaRecargoAnt](input)

  override def processMessage(registro: ParametricaRecargoAnt): Future[Response.SuccessProcessing] = {

    val command = ParametricaRecargoCommands.ParametricaRecargoUpdateFromDto(
      parametricaRecargoId = registro.BPR_INDICE,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )

    actor ! command
    Future.successful(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }

}
