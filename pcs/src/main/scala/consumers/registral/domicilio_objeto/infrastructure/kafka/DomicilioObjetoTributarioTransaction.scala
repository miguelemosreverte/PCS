package consumers.registral.domicilio_objeto.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.{Config, ConfigFactory}
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoCommands
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoExternalDto.{
  DomicilioObjetoAnt,
  DomicilioObjetoTri
}
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decodeF, maybeDecode}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class DomicilioObjetoTributarioTransaction(actor: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[DomicilioObjetoTri](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-DOMICILIO-OBJ-TRI"

  def processInput(input: String): Either[Throwable, DomicilioObjetoTri] =
    maybeDecode[DomicilioObjetoTri](input)

  override def processMessage(registro: DomicilioObjetoTri): Future[Response.SuccessProcessing] = {
    val command = DomicilioObjetoCommands.DomicilioObjetoUpdateFromDto(
      sujetoId = registro.BDO_SUJ_IDENTIFICADOR,
      objetoId = registro.BDO_SOJ_IDENTIFICADOR,
      tipoObjeto = registro.BDO_SOJ_TIPO_OBJETO,
      domicilioId = registro.BDO_DOM_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor ask command
  }

}
