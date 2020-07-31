package consumers.registral.declaracion_jurada.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaCommands
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaExternalDto.DeclaracionJurada
import consumers.registral.declaracion_jurada.infrastructure.dependency_injection.DeclaracionJuradaActor
import consumers.registral.declaracion_jurada.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.{ExecutionContext, Future}

case class DeclaracionJuradaTransaction(actor: DeclaracionJuradaActor, monitoring: Monitoring)(implicit
                                                                                               system: ActorSystem[_],
                                                                                               ec: ExecutionContext)
    extends ActorTransaction[DeclaracionJurada](monitoring) {
  val topic = "DGR-COP-DECJURADAS"

  override def processCommand(registro: DeclaracionJurada): Future[Done] = {
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
