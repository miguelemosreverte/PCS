package consumers.registral.tramite.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import consumers.registral.tramite.application.entities.TramiteExternalDto.Tramite
import consumers.registral.tramite.application.entities.{TramiteCommands, TramiteExternalDto}
import consumers.registral.tramite.infrastructure.dependency_injection.TramiteActor
import consumers.registral.tramite.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import serialization.decodeF

import scala.concurrent.Future

case class TramiteTransaction()(implicit actor: TramiteActor, system: akka.actor.typed.ActorSystem[_])
    extends ActorTransaction {

  val topic = "DGR-COP-TRAMITES"

  override def transaction(input: String): Future[Done] = {
    val registro: Tramite = decodeF[Tramite](input)
    val command = registro match {
      case registro: TramiteExternalDto.Tramite =>
        TramiteCommands.TramiteUpdateFromDto(
          sujetoId = registro.BTR_SUJ_IDENTIFICADOR,
          tramiteId = registro.BTR_TRMID,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }

}
