package consumers.no_registral.sujeto.infrastructure.consumer

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoTri
import consumers.no_registral.sujeto.application.entity.{SujetoCommands, SujetoExternalDto}
import consumers.no_registral.sujeto.infrastructure.json._
import serialization.decodeF

import scala.concurrent.Future

case class SujetoTributarioTransaction()(implicit actorRef: ActorRef) extends ActorTransaction {

  val topic = "DGR-COP-SUJETO-TRI"

  override def transaction(input: String): Future[Done] = {
    val registro: SujetoTri = decodeF[SujetoTri](input)

    val command = registro match {
      case registro: SujetoExternalDto.SujetoTri =>
        SujetoCommands.SujetoUpdateFromTri(
          sujetoId = registro.SUJ_IDENTIFICADOR,
          deliveryId = registro.EV_ID,
          registro = registro
        )
    }
    actorRef.ask[akka.Done](command)
  }
}
