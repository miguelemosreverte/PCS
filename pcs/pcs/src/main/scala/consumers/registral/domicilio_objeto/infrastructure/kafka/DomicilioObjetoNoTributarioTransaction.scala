package consumers.registral.domicilio_objeto.infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import api.actor_transaction.ActorTransaction
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoCommands
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoExternalDto.DomicilioObjetoAnt
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import serialization.decodeF

import scala.concurrent.Future

case class DomicilioObjetoNoTributarioTransaction()(implicit actor: DomicilioObjetoActor, system: ActorSystem[_])
    extends ActorTransaction {

  val topic = "DGR-COP-DOMICILIO-OBJ-ANT"

  override def transaction(input: String): Future[Done] = {
    val registro: DomicilioObjetoAnt = decodeF[DomicilioObjetoAnt](input)
    val command = registro match {
      case registro: DomicilioObjetoAnt =>
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
