package consumers.no_registral.sujeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoTri
import consumers.no_registral.sujeto.application.entity.{SujetoCommands, SujetoExternalDto}
import consumers.no_registral.sujeto.infrastructure.json._
import monitoring.Monitoring
import serialization.decodeF

case class SujetoTributarioTransaction(monitoring: Monitoring)(implicit actorRef: ActorRef, ec: ExecutionContext)
    extends ActorTransaction[SujetoTri](monitoring) {

  val topic = "DGR-COP-SUJETO-TRI"

  def processCommand(registro: SujetoTri): Future[Done] = {
    val command = registro match {
      case _: SujetoExternalDto.SujetoTri =>
        SujetoCommands.SujetoUpdateFromTri(
          sujetoId = registro.SUJ_IDENTIFICADOR,
          deliveryId = registro.EV_ID,
          registro = registro
        )
    }
    actorRef.ask[akka.Done](command)
  }
}
