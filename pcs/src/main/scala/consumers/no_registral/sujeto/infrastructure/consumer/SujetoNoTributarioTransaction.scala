package consumers.no_registral.sujeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoAnt
import consumers.no_registral.sujeto.infrastructure.json._
import serialization.decodeF

case class SujetoNoTributarioTransaction()(implicit actorRef: ActorRef, ec: ExecutionContext) extends ActorTransaction {

  val topic = "DGR-COP-SUJETO-ANT"

  override def transaction(input: String): Future[Done] =
    for {
      cmd <- processInput(input)
      done <- processCommand(cmd)
    } yield done

  def processInput(input: String): Future[SujetoAnt] = Future {
    decodeF[SujetoAnt](input)
  }

  def processCommand(registro: SujetoAnt): Future[Done] = {
    val command = SujetoCommands.SujetoUpdateFromAnt(
      sujetoId = registro.SUJ_IDENTIFICADOR,
      deliveryId = registro.EV_ID,
      registro = registro
    )
    actorRef.ask[akka.Done](command)
  }
}
