package consumers.registral.domicilio_sujeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoCommands
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoExternalDto.DomicilioSujetoAnt
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import serialization.decodeF

import scala.concurrent.Future

case class DomicilioSujetoNoTributarioTransaction()(implicit actor: DomicilioSujetoActor, system: ActorSystem[_])
    extends ActorTransaction {

  val topic = "DGR-COP-DOMICILIO-SUJ-ANT"

  override def transaction(input: String): Future[Done] = {
    val registro: DomicilioSujetoAnt = decodeF[DomicilioSujetoAnt](input)
    val command = registro match {
      case registro: DomicilioSujetoAnt =>
        DomicilioSujetoCommands.DomicilioSujetoUpdateFromDto(
          sujetoId = registro.BDS_SUJ_IDENTIFICADOR,
          domicilioId = registro.BDS_DOM_ID,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }

}
