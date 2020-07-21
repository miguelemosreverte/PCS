package consumers.no_registral.sujeto.infrastructure.consumer

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoAnt
import consumers.no_registral.sujeto.infrastructure.json._
import serialization.decodeF

import scala.concurrent.Future

case class SujetoNoTributarioTransaction()(implicit actorRef: ActorRef) extends ActorTransaction {

  val topic = "DGR-COP-SUJETO-ANT"

  override def transaction(input: String): Future[Done] = {
    val registro: SujetoAnt = decodeF[SujetoAnt](input)

    val command = SujetoCommands.SujetoUpdateFromAnt(
      sujetoId = registro.SUJ_IDENTIFICADOR,
      deliveryId = registro.EV_ID,
      registro = registro
    )
    actorRef.ask[akka.Done](command)
  }
}
