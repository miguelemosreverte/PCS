package consumers.no_registral.sujeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.{Actor, ActorRef}
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.{SujetoAnt, SujetoTri}
import consumers.no_registral.sujeto.application.entity.{SujetoCommands, SujetoExternalDto}
import consumers.no_registral.sujeto.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import monitoring.Monitoring
import serialization.decode2

import scala.util.Try

case class SujetoTributarioTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[SujetoTri](monitoring) {

  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-SUJETO-TRI"

  def processInput(input: String): Either[Throwable, SujetoTri] =
    decode2[SujetoTri](input)

  def processCommand(registro: SujetoTri): Future[Response.SuccessProcessing] = {
    val command = registro match {
      case _: SujetoExternalDto.SujetoTri =>
        SujetoCommands.SujetoUpdateFromTri(
          sujetoId = registro.SUJ_IDENTIFICADOR,
          deliveryId = registro.EV_ID,
          registro = registro
        )
    }

    /*class Amiguito extends Actor {
      ove
    }*/
    actorRef ! command
    Future.successful(SuccessProcessing(command.deliveryId))
    //actorRef.ask[Response.SuccessProcessing](command)

  }
}
