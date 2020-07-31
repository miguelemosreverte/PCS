package consumers.no_registral.sujeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoTri
import consumers.no_registral.sujeto.application.entity.{SujetoCommands, SujetoExternalDto}
import consumers.no_registral.sujeto.infrastructure.json._
import monitoring.{KamonMonitoring, Monitoring}
import serialization.decodeF

case class SujetoTributarioTransaction(actorRef: ActorRef, monitoring: Monitoring)(implicit ec: ExecutionContext)
    extends ActorTransaction[SujetoTri](monitoring) {
  val SujetoTributarioTransactionSuccessCounter =
    new KamonMonitoring().counter("SujetoTributarioTransactionSuccessCounter")

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
    for {
      done <- actorRef.ask[akka.Done](command)
      _ = SujetoTributarioTransactionSuccessCounter.increment()
    } yield done
  }
}
