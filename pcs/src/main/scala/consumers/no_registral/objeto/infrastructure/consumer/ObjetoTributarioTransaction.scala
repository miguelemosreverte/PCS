package consumers.no_registral.objeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{ObjetosTri, ObjetosTriOtrosAtributos}
import consumers.no_registral.objeto.infrastructure.json._
import play.api.libs.json.Reads
import serialization.decodeF

case class ObjetoTributarioTransaction()(implicit actorRef: ActorRef, ec: ExecutionContext) extends ActorTransaction {

  val topic = "DGR-COP-OBJETOS-TRI"

  override def transaction(input: String): Future[Done] =
    for {
      cmd <- processInput(input)
      done <- processCommand(cmd)
    } yield done

  def processInput(input: String): Future[ObjetosTri] = Future {
    decodeF[ObjetosTri](input)
  }

  def processCommand(registro: ObjetosTri): Future[Done] = {
    implicit val a: Reads[Seq[ObjetosTri]] = Reads.seq(ObjetosTriF.reads)
    implicit val b: Reads[Seq[ObjetosTriOtrosAtributos]] = Reads.seq(ObjetosTriOtrosAtributosF.reads)

    val detalle: Option[ObjetosTriOtrosAtributos] = for {
      otrosAtributos <- registro.SOJ_OTROS_ATRIBUTOS
      sojDetalles <- (otrosAtributos \ "SOJ_DETALLES").toOption
      detalles <- serialization.decodeF[Seq[ObjetosTriOtrosAtributos]](sojDetalles.toString).headOption
    } yield detalles

    val isResponsable = detalle map { d =>
      d.RESPONSABLE_OTROS_ATRIBUTOS contains "S"
    }
    val sujetoResponsable = detalle flatMap { d =>
      d.RESPONSABLE_OTROS_ATRIBUTOS.getOrElse("N") match {
        case "S" => Some(registro.SOJ_SUJ_IDENTIFICADOR)
        case "N" => None
      }
    }

    val command: ObjetoCommands =
      if (registro.SOJ_ESTADO.contains("BAJA"))
        ObjetoCommands.SetBajaObjeto(
          sujetoId = registro.SOJ_SUJ_IDENTIFICADOR,
          objetoId = registro.SOJ_IDENTIFICADOR,
          tipoObjeto = registro.SOJ_TIPO_OBJETO,
          deliveryId = registro.EV_ID,
          registro = registro,
          isResponsable = isResponsable,
          sujetoResponsable = sujetoResponsable
        )
      else
        ObjetoCommands.ObjetoUpdateFromTri(
          sujetoId = registro.SOJ_SUJ_IDENTIFICADOR,
          objetoId = registro.SOJ_IDENTIFICADOR,
          tipoObjeto = registro.SOJ_TIPO_OBJETO,
          deliveryId = registro.EV_ID,
          registro = registro,
          isResponsable = isResponsable,
          sujetoResponsable = sujetoResponsable
        )

    actorRef.ask[akka.Done](command)
  }
}
