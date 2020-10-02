package consumers.registral.declaracion_jurada.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.calendario.application.entities.CalendarioExternalDto
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaCommands
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaExternalDto.DeclaracionJurada
import consumers.registral.declaracion_jurada.infrastructure.dependency_injection.DeclaracionJuradaActor
import consumers.registral.declaracion_jurada.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class DeclaracionJuradaTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[DeclaracionJurada](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-DECJURADAS"

  def processInput(input: String): Either[Throwable, DeclaracionJurada] =
    maybeDecode[DeclaracionJurada](input)

  override def processMessage(registro: DeclaracionJurada): Future[Response.SuccessProcessing] = {
    val command = DeclaracionJuradaCommands.DeclaracionJuradaUpdateFromDto(
      sujetoId = registro.BDJ_SUJ_IDENTIFICADOR,
      objetoId = registro.BDJ_SOJ_IDENTIFICADOR,
      tipoObjeto = registro.BDJ_SOJ_TIPO_OBJETO,
      declaracionJuradaId = registro.BDJ_DDJ_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )

    actor ask command
  }

}
