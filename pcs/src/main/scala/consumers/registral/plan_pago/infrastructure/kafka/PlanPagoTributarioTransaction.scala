package consumers.registral.plan_pago.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.plan_pago.application.entities.PlanPagoExternalDto.{PlanPagoAnt, PlanPagoTri}
import consumers.registral.plan_pago.application.entities.{PlanPagoCommands, PlanPagoExternalDto}
import consumers.registral.plan_pago.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class PlanPagoTributarioTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[PlanPagoTri](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-PLANES-TRI"

  def processInput(input: String): Either[Throwable, PlanPagoTri] =
    maybeDecode[PlanPagoTri](input)

  override def processMessage(registro: PlanPagoTri): Future[Response.SuccessProcessing] = {
    val command = PlanPagoCommands.PlanPagoUpdateFromDto(
      sujetoId = registro.BPL_SUJ_IDENTIFICADOR,
      objetoId = registro.BPL_SOJ_IDENTIFICADOR,
      tipoObjeto = registro.BPL_SOJ_TIPO_OBJETO,
      planPagoId = registro.BPL_PLN_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )

    actor ! command
    Future.successful(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
