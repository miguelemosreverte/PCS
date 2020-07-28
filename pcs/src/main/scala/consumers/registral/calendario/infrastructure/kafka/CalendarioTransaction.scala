package consumers.registral.calendario.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import consumers.registral.calendario.application.entities.{CalendarioCommands, CalendarioExternalDto}
import consumers.registral.calendario.infrastructure.dependency_injection.CalendarioActor
import consumers.registral.calendario.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.Future

case class CalendarioTransaction(monitoring: Monitoring)(implicit actor: CalendarioActor, system: ActorSystem[_])
    extends ActorTransaction(monitoring) {

  val topic = "DGR-COP-CALENDARIOS"

  override def transaction(input: String): Future[Done] = {
    val registro: CalendarioExternalDto = decodeF[CalendarioExternalDto](input)
    val command = registro match {
      case registro: CalendarioExternalDto =>
        CalendarioCommands.CalendarioUpdateFromDto(
          calendarioId = registro.BCL_IDENTIFICADOR,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }

}
