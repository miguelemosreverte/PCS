package consumers.registral.domicilio_objeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoCommands
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoExternalDto.DomicilioObjetoTri
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.Future

case class DomicilioObjetoTributarioTransaction(monitoring: Monitoring)(implicit actor: DomicilioObjetoActor,
                                                                        system: ActorSystem[_])
    extends ActorTransaction(monitoring) {

  val topic = "DGR-COP-DOMICILIO-OBJ-TRI"

  override def transaction(input: String): Future[Done] = {
    val registro: DomicilioObjetoTri = decodeF[DomicilioObjetoTri](input)
    val command = registro match {
      case registro: DomicilioObjetoTri =>
        DomicilioObjetoCommands.DomicilioObjetoUpdateFromDto(
          sujetoId = registro.BDO_SUJ_IDENTIFICADOR,
          objetoId = registro.BDO_SOJ_IDENTIFICADOR,
          tipoObjeto = registro.BDO_SOJ_TIPO_OBJETO,
          domicilioId = registro.BDO_DOM_ID,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }

}
