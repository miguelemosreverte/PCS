package consumers.registral.subasta.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.plan_pago.application.entities.PlanPagoExternalDto.PlanPagoTri
import consumers.registral.subasta.application.entities.{SubastaCommands, SubastaExternalDto}
import consumers.registral.subasta.infrastructure.dependency_injection.SubastaActor
import consumers.registral.subasta.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class SubastaTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[SubastaExternalDto](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-SUBASTAS"

  def processInput(input: String): Either[Throwable, SubastaExternalDto] =
    maybeDecode[SubastaExternalDto](input)

  override def processMessage(registro: SubastaExternalDto): Future[Response.SuccessProcessing] = {
    val command = SubastaCommands.SubastaUpdateFromDto(
      sujetoId = registro.BSB_SUJ_IDENTIFICADOR_ADQ,
      objetoId = registro.BSB_SOJ_IDENTIFICADOR,
      tipoObjeto = registro.BSB_SOJ_TIPO_OBJETO,
      subastaId = registro.BSB_SUB_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )

    actor ask command
  }

}
