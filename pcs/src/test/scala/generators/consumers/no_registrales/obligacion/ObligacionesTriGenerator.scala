package generators.consumers.no_registrales.obligacion

import java.util.concurrent.atomic.AtomicInteger

import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionUpdateFromDto
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.{
  DetallesObligacion,
  ObligacionesAnt,
  ObligacionesTri
}
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionUpdatedFromDto
import consumers.no_registral.obligacion.infrastructure.json.DetallesObligacionF
import generators.consumers.no_registrales.{Generator, Helper}
import play.api.libs.json.Reads
import stubs.consumers.no_registrales.obligacion.ObligacionCommands.detallesObligaciones
import stubs.loadExample
import utils.generators.Model.deliveryId
import consumers.no_registral.obligacion.infrastructure.json._

import scala.util.Random

class ObligacionesTriGenerator extends Generator[ObligacionesTri] {
  import ObligacionesTriGenerator._
  lazy val example: ObligacionesTri = loadExample[ObligacionesTri]("assets/examples/DGR-COP-OBLIGACIONES-TRI.json")

  def next: ObligacionesTri = {
    val id = i.incrementAndGet().toString
    example.copy(
      EV_ID = id.toInt,
      BOB_SUJ_IDENTIFICADOR = id,
      BOB_SOJ_IDENTIFICADOR = id,
      BOB_OBN_ID = id
    )
  }

  override def toJson(e: ObligacionesTri): String = e.toJson

  override def aggregateRoot(e: ObligacionesTri): String = e.aggregateRoot

}

object ObligacionesTriGenerator {
  implicit class ObligacionesTriGeneratorHelper(obligacionesTri: ObligacionesTri) extends Helper {

    def toJson: String =
      serialization.encode(obligacionesTri)

    def toEvent: ObligacionUpdatedFromDto = {
      ObligacionUpdatedFromDto(
        obligacionesTri.BOB_SUJ_IDENTIFICADOR,
        obligacionesTri.BOB_SOJ_IDENTIFICADOR,
        obligacionesTri.BOB_SOJ_TIPO_OBJETO,
        obligacionesTri.BOB_OBN_ID,
        obligacionesTri,
        detallesObligaciones.getOrElse(Seq.empty)
      )
    }

    def toCommand: ObligacionUpdateFromDto = {
      implicit val b: Reads[Seq[DetallesObligacion]] =
        Reads.seq(DetallesObligacionF.reads)

      val detallesObligaciones: Option[Seq[DetallesObligacion]] = for {
        otrosAtributos <- obligacionesTri.BOB_OTROS_ATRIBUTOS
        bjuDetalles <- (otrosAtributos \ "BOB_DETALLES").toOption
        detalles = serialization.decodeF[Seq[DetallesObligacion]](bjuDetalles.toString)
      } yield detalles

      def obligacionUpdatedFromDtoStub =
        ObligacionUpdateFromDto(
          obligacionesTri.BOB_SUJ_IDENTIFICADOR,
          obligacionesTri.BOB_SOJ_IDENTIFICADOR,
          obligacionesTri.BOB_SOJ_TIPO_OBJETO,
          obligacionesTri.BOB_OBN_ID,
          deliveryId,
          obligacionesTri,
          detallesObligaciones.getOrElse(Seq.empty)
        )

      obligacionUpdatedFromDtoStub
    }

  }
}
