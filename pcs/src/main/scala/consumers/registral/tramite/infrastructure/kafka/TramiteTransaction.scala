package consumers.registral.tramite.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import consumers.registral.tramite.application.entities.TramiteExternalDto.Tramite
import consumers.registral.tramite.application.entities.{TramiteCommands, TramiteExternalDto}
import consumers.registral.tramite.infrastructure.dependency_injection.TramiteActor
import consumers.registral.tramite.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.{ExecutionContext, Future}

case class TramiteTransaction()(implicit actor: TramiteActor,
                                system: akka.actor.typed.ActorSystem[_],
                                monitoring: Monitoring,
                                ec: ExecutionContext)
    extends ActorTransaction[Tramite](monitoring) {

  val topic = "DGR-COP-TRAMITES"

  override def processCommand(registro: Tramite): Future[Done] = {
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
