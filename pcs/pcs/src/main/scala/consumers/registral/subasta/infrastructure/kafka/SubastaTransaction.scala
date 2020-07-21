package consumers.registral.subasta.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import consumers.registral.subasta.application.entities.{SubastaCommands, SubastaExternalDto}
import consumers.registral.subasta.infrastructure.dependency_injection.SubastaActor
import consumers.registral.subasta.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import serialization.decodeF

import scala.concurrent.Future

case class SubastaTransaction()(implicit actor: SubastaActor, system: akka.actor.typed.ActorSystem[_])
    extends ActorTransaction {

  val topic = "DGR-COP-SUBASTAS"

  override def transaction(input: String): Future[Done] = {
    val registro: SubastaExternalDto = decodeF[SubastaExternalDto](input)
    val command = registro match {
      case registro: SubastaExternalDto =>
        SubastaCommands.SubastaUpdateFromDto(
          sujetoId = registro.BSB_SUJ_IDENTIFICADOR_ADQ,
          objetoId = registro.BSB_SOJ_IDENTIFICADOR,
          tipoObjeto = registro.BSB_SOJ_TIPO_OBJETO,
          subastaId = registro.BSB_SUB_ID,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }

}
