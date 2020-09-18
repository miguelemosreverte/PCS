package consumers.registral.parametrica_plan.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanExternalDto.{
  ParametricaPlanAnt,
  ParametricaPlanTri
}
import consumers.registral.parametrica_plan.application.entities.{ParametricaPlanCommands, ParametricaPlanExternalDto}
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class ParametricaPlanTributarioTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ParametricaPlanTri](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-PARAMPLAN-TRI"

  def processInput(input: String): Either[Throwable, ParametricaPlanTri] =
    maybeDecode[ParametricaPlanTri](input)

  override def processMessage(registro: ParametricaPlanTri): Future[Response.SuccessProcessing] = {

    val command = ParametricaPlanCommands.ParametricaPlanUpdateFromDto(
      parametricaPlanId = registro.BPP_FPM_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor ask command
  }

}
