package consumers.no_registral.obligacion.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.{
  DetallesObligacion,
  ObligacionesAnt,
  ObligacionesTri
}
import consumers.no_registral.obligacion.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import monitoring.Monitoring
import play.api.libs.json.Reads
import serialization.{decodeF, maybeDecode}

import scala.util.Try

case class ObligacionTributariaTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObligacionesTri](monitoring) {

  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-OBLIGACIONES-TRI"

  def processInput(input: String): Either[Throwable, ObligacionesTri] =
    maybeDecode[ObligacionesTri](input)

  def processMessage(registro: ObligacionesTri): Future[Response.SuccessProcessing] = {
    implicit val b: Reads[Seq[DetallesObligacion]] = Reads.seq(DetallesObligacionF.reads)

    val detalles: Option[Seq[DetallesObligacion]] = for {
      otrosAtributos <- registro.BOB_OTROS_ATRIBUTOS
      bobDetalles <- (otrosAtributos \ "BOB_DETALLES").toOption
      detalles = serialization.decodeF[Seq[DetallesObligacion]](bobDetalles.toString)
    } yield detalles

    val command =
      if (registro.BOB_ESTADO.contains("BAJA")) {
        ObligacionCommands.DownObligacion(
          sujetoId = registro.BOB_SUJ_IDENTIFICADOR,
          objetoId = registro.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = registro.BOB_SOJ_TIPO_OBJETO,
          obligacionId = registro.BOB_OBN_ID,
          deliveryId = registro.EV_ID
        )
      } else
        ObligacionCommands.ObligacionUpdateFromDto(
          sujetoId = registro.BOB_SUJ_IDENTIFICADOR,
          objetoId = registro.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = registro.BOB_SOJ_TIPO_OBJETO,
          obligacionId = registro.BOB_OBN_ID,
          deliveryId = registro.EV_ID,
          registro = registro,
          detallesObligacion = detalles.getOrElse(Seq.empty)
        )
    actorRef.ask[Response.SuccessProcessing](command)

  }
}
