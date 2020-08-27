package consumers.registral.domicilio_objeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
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
import serialization.{decode2, decodeF}

import scala.concurrent.{ExecutionContext, Future}

case class DomicilioObjetoTributarioTransaction(actor: DomicilioObjetoActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[DomicilioObjetoTri](monitoring) {

  val topic = "DGR-COP-DOMICILIO-OBJ-TRI"

  def processInput(input: String): Either[Throwable, DomicilioObjetoTri] =
    decode2[DomicilioObjetoTri](input)

  override def processCommand(registro: DomicilioObjetoTri): Future[Response.SuccessProcessing] = {
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
