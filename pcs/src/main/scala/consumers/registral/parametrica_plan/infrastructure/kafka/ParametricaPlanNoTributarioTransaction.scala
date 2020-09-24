package consumers.registral.parametrica_plan.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.juicio.application.entities.JuicioExternalDto.JuicioTri
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanExternalDto.ParametricaPlanAnt
import consumers.registral.parametrica_plan.application.entities.{ParametricaPlanCommands, ParametricaPlanExternalDto}
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class ParametricaPlanNoTributarioTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ParametricaPlanAnt](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-PARAMPLAN-ANT"

  def processInput(input: String): Either[Throwable, ParametricaPlanAnt] =
    maybeDecode[ParametricaPlanAnt](input)

  override def processMessage(registro: ParametricaPlanAnt): Future[Response.SuccessProcessing] = {
    val command = ParametricaPlanCommands.ParametricaPlanUpdateFromDto(
      parametricaPlanId = registro.BPP_FPM_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )

    actor ! command
    Future.successful(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }

}
