package consumers.registral.domicilio_sujeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoCommands
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoExternalDto.DomicilioSujetoTri
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.Future

case class DomicilioSujetoTributarioTransaction(monitoring: Monitoring)(implicit actor: DomicilioSujetoActor,
                                                                        system: ActorSystem[_])
    extends ActorTransaction(monitoring) {

  val topic = "DGR-COP-DOMICILIO-SUJ-TRI"

  override def transaction(input: String): Future[Done] = {
    val registro: DomicilioSujetoTri = decodeF[DomicilioSujetoTri](input)
    val command = registro match {
      case registro: DomicilioSujetoTri =>
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
