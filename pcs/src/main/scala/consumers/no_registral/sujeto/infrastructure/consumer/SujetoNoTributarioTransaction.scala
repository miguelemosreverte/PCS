package consumers.no_registral.sujeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoAnt
import consumers.no_registral.sujeto.infrastructure.json._
import monitoring.Monitoring
import serialization.decodeF

case class SujetoNoTributarioTransaction()(implicit actorRef: ActorRef, ec: ExecutionContext, monitoring: Monitoring)
    extends ActorTransaction[SujetoAnt](monitoring) {

  val topic = "DGR-COP-SUJETO-ANT"

  override def processCommand(registro: SujetoAnt): Future[Done] = {
    val command = SujetoCommands.SujetoUpdateFromAnt(
      sujetoId = registro.SUJ_IDENTIFICADOR,
      deliveryId = registro.EV_ID,
      registro = registro
    )
    actorRef.ask[akka.Done](command)
  }
}
