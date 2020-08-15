package consumers.no_registral.sujeto.infrastructure.consumer

import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoTri
import consumers.no_registral.sujeto.application.entity.{SujetoCommands, SujetoExternalDto}
import consumers.no_registral.sujeto.infrastructure.json._
import design_principles.actor_model.Response
import monitoring.Monitoring
import play.api.libs.json.Format
import serialization.decode

import scala.io.Source
import scala.reflect.ClassTag

case class SujetoTributarioTransaction(actorRef: ActorRef, monitoring: Monitoring)(implicit ec: ExecutionContext)
    extends ActorTransaction[SujetoTri](monitoring) {

  val topic = "DGR-COP-SUJETO-TRI"

  def processCommand(registro: SujetoTri): Future[Response.SuccessProcessing] = {
    val command = registro match {
      case _: SujetoExternalDto.SujetoTri =>
        SujetoCommands.SujetoUpdateFromTri(
          sujetoId = registro.SUJ_IDENTIFICADOR,
          deliveryId = registro.EV_ID,
          registro = registro
        )
    }
    actorRef.ask[Response.SuccessProcessing](command)
  }
}
