package consumers.no_registral.obligacion.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.{
  DetallesObligacion,
  ObligacionesAnt
}
import consumers.no_registral.obligacion.infrastructure.json._
import monitoring.Monitoring
import play.api.libs.json.Reads
import serialization.decodeF

case class ObligacionNoTributariaTransaction(monitoring: Monitoring)(implicit actorRef: ActorRef, ec: ExecutionContext)
    extends ActorTransaction(monitoring) {

  val topic = "DGR-COP-OBLIGACIONES-ANT"

  override def transaction(input: String): Future[Done] =
    for {
      cmd <- processInput(input)
      done <- processCommand(cmd)
    } yield done

  def processInput(input: String): Future[ObligacionesAnt] = Future {
    decodeF[ObligacionesAnt](input)
  }

  def processCommand(registro: ObligacionesAnt): Future[Done] = {
    implicit val b: Reads[Seq[DetallesObligacion]] = Reads.seq(DetallesObligacionF.reads)

    val detalles: Option[Seq[DetallesObligacion]] = for {
      otrosAtributos <- registro.BOB_OTROS_ATRIBUTOS
      bobDetalles <- (otrosAtributos \ "BOB_DETALLES").toOption
      detalles = serialization.decodeF[Seq[DetallesObligacion]](bobDetalles.toString)
    } yield detalles

    {}

    val command =
      if (registro.BOB_ESTADO.contains("BAJA"))
        ObligacionCommands.DownObligacion(
          sujetoId = registro.BOB_SUJ_IDENTIFICADOR,
          objetoId = registro.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = registro.BOB_SOJ_TIPO_OBJETO,
          obligacionId = registro.BOB_OBN_ID,
          deliveryId = registro.EV_ID
        )
      else
        ObligacionCommands.ObligacionUpdateFromDto(
          sujetoId = registro.BOB_SUJ_IDENTIFICADOR,
          objetoId = registro.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = registro.BOB_SOJ_TIPO_OBJETO,
          obligacionId = registro.BOB_OBN_ID,
          deliveryId = registro.EV_ID,
          registro = registro,
          detallesObligacion = detalles.getOrElse(Seq.empty)
        )
    actorRef.ask[akka.Done](command)
  }
}
