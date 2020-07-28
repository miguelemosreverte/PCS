package consumers.registral.etapas_procesales.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesExternalDto.EtapasProcesalesAnt
import consumers.registral.etapas_procesales.application.entities.{
  EtapasProcesalesCommands,
  EtapasProcesalesExternalDto
}
import consumers.registral.etapas_procesales.infrastructure.dependency_injection.EtapasProcesalesActor
import consumers.registral.etapas_procesales.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.decodeF

import scala.concurrent.Future

case class EtapasProcesalesNoTributarioTransaction(monitoring: Monitoring)(implicit actor: EtapasProcesalesActor,
                                                                           system: akka.actor.typed.ActorSystem[_])
    extends ActorTransaction(monitoring) {

  val topic = "DGR-COP-ETAPROCESALES-ANT"

  override def transaction(input: String): Future[Done] = {
    val registro: EtapasProcesalesAnt = decodeF[EtapasProcesalesAnt](input)
    val command = registro match {
      case registro: EtapasProcesalesExternalDto.EtapasProcesalesAnt =>
        EtapasProcesalesCommands.EtapasProcesalesUpdateFromDto(
          juicioId = registro.BEP_JUI_ID,
          etapaId = registro.BPE_ETA_ID,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor ask command
  }

}
