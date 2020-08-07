package consumers.no_registral.sujeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoAnt
import consumers.no_registral.sujeto.infrastructure.json._
import design_principles.actor_model.Response
import monitoring.Monitoring
import serialization.decodeF

case class SujetoNoTributarioTransaction(actorRef: ActorRef, monitoring: Monitoring)(implicit ec: ExecutionContext)
    extends ActorTransaction[SujetoAnt](monitoring) {

  val topic = "DGR-COP-SUJETO-ANT"

  def processCommand(registro: SujetoAnt): Future[Response.SuccessProcessing] = {
    val command = SujetoCommands.SujetoUpdateFromAnt(
      sujetoId = registro.SUJ_IDENTIFICADOR,
      deliveryId = registro.EV_ID,
      registro = registro
    )
    actorRef.ask[Response.SuccessProcessing](command)
  }
}
