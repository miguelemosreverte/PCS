package consumers.registral.etapas_procesales.infrastructure.kafka

import akka.Done
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesExternalDto.{
  EtapasProcesalesAnt,
  EtapasProcesalesTri
}
import consumers.registral.etapas_procesales.application.entities.{
  EtapasProcesalesCommands,
  EtapasProcesalesExternalDto
}
import consumers.registral.etapas_procesales.infrastructure.dependency_injection.EtapasProcesalesActor
import consumers.registral.etapas_procesales.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import serialization.{decode2, decodeF}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class EtapasProcesalesTributarioTransaction(actor: EtapasProcesalesActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[EtapasProcesalesTri](monitoring) {
  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-ETAPROCESALES-TRI"

  def processInput(input: String): Either[Throwable, EtapasProcesalesTri] =
    decode2[EtapasProcesalesTri](input)

  override def processCommand(registro: EtapasProcesalesTri): Future[Response.SuccessProcessing] = {
    val command = EtapasProcesalesCommands.EtapasProcesalesUpdateFromDto(
      juicioId = registro.BEP_JUI_ID,
      etapaId = registro.BPE_ETA_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor ask command
  }

}
