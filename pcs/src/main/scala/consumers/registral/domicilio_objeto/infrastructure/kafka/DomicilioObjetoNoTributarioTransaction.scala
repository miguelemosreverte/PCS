package consumers.registral.domicilio_objeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaExternalDto.DeclaracionJurada
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoCommands
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoExternalDto.DomicilioObjetoAnt
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decode2, decodeF}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class DomicilioObjetoNoTributarioTransaction(actor: DomicilioObjetoActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[DomicilioObjetoAnt](monitoring) {
  def topic = "DGR-COP-DOMICILIO-OBJ-ANT"
  Try {
    actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
  } getOrElse "DGR-COP-DOMICILIO-OBJ-ANT"

  def processInput(input: String): Either[Throwable, DomicilioObjetoAnt] =
    decode2[DomicilioObjetoAnt](input)

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
