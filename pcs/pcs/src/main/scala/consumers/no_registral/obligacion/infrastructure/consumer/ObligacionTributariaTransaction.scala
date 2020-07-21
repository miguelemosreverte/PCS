package consumers.no_registral.obligacion.infrastructure.consumer

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.{
  DetallesObligacion,
  ObligacionesTri
}
import consumers.no_registral.obligacion.infrastructure.json._
import play.api.libs.json.Reads
import serialization.decodeF

import scala.concurrent.Future

case class ObligacionTributariaTransaction()(implicit actorRef: ActorRef) extends ActorTransaction {

  val topic = "DGR-COP-OBLIGACIONES-TRI"

  override def transaction(input: String): Future[Done] = {
    implicit val b: Reads[Seq[DetallesObligacion]] = Reads.seq(DetallesObligacionF.reads)

    val registro: ObligacionesTri = decodeF[ObligacionesTri](input)
    val detalles: Option[Seq[DetallesObligacion]] = for {
      otrosAtributos <- registro.BOB_OTROS_ATRIBUTOS
      bobDetalles <- (otrosAtributos \ "BOB_DETALLES").toOption
      detalles = serialization.decodeF[Seq[DetallesObligacion]](bobDetalles.toString)
    } yield detalles

    val command = ObligacionCommands.ObligacionUpdateFromDto(
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
