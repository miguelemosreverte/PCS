package consumers.registral.domicilio_objeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoCommands
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoExternalDto.DomicilioObjetoAnt
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.{ExecutionContext, Future}

case class DomicilioObjetoNoTributarioTransaction(actor: DomicilioObjetoActor, monitoring: Monitoring)(
    implicit
    system: ActorSystem[_],
    ec: ExecutionContext
) extends ActorTransaction[DomicilioObjetoAnt](monitoring) {

  val topic = "DGR-COP-DOMICILIO-OBJ-ANT"

  override def processCommand(registro: DomicilioObjetoAnt): Future[Response.SuccessProcessing] = {
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
