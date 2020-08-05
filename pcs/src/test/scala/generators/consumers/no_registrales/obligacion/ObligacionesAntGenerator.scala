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
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoAnt

class ObligacionesAntGenerator extends Generator[ObligacionesAnt] {
  import ObligacionesAntGenerator._

  lazy val example: ObligacionesAnt = loadExample[ObligacionesAnt]("assets/examples/DGR-COP-OBLIGACIONES-ANT.json")

  def next: ObligacionesAnt = {
    val id = i.incrementAndGet().toString
    example.copy(
      EV_ID = deliveryId,
      BOB_SUJ_IDENTIFICADOR = id,
      BOB_SOJ_IDENTIFICADOR = id,
      BOB_OBN_ID = id
    )
  }

  override def toJson(e: ObligacionesAnt): String = e.toJson

  override def aggregateRoot(e: ObligacionesAnt): String = e.aggregateRoot
}

object ObligacionesAntGenerator {
  implicit class ObligacionesAntGeneratorHelper(obligacionesAnt: ObligacionesAnt) extends Helper {

    def toJson: String =
      serialization.encode(obligacionesAnt)

    def toEvent: ObligacionUpdatedFromDto = {
      ObligacionUpdatedFromDto(
        obligacionesAnt.BOB_SUJ_IDENTIFICADOR,
        obligacionesAnt.BOB_SOJ_IDENTIFICADOR,
        obligacionesAnt.BOB_SOJ_TIPO_OBJETO,
        obligacionesAnt.BOB_OBN_ID,
        obligacionesAnt,
        detallesObligaciones.getOrElse(Seq.empty)
      )
    }

    def toCommand: ObligacionUpdateFromDto = {
      implicit val b: Reads[Seq[DetallesObligacion]] =
        Reads.seq(DetallesObligacionF.reads)

      val detallesObligaciones: Option[Seq[DetallesObligacion]] = for {
        otrosAtributos <- obligacionesAnt.BOB_OTROS_ATRIBUTOS
        bjuDetalles <- (otrosAtributos \ "BOB_DETALLES").toOption
        detalles = serialization.decodeF[Seq[DetallesObligacion]](bjuDetalles.toString)
      } yield detalles

      def obligacionUpdatedFromDtoStub =
        ObligacionUpdateFromDto(
          obligacionesAnt.BOB_SUJ_IDENTIFICADOR,
          obligacionesAnt.BOB_SOJ_IDENTIFICADOR,
          obligacionesAnt.BOB_SOJ_TIPO_OBJETO,
          obligacionesAnt.BOB_OBN_ID,
          deliveryId,
          obligacionesAnt,
          detallesObligaciones.getOrElse(Seq.empty)
        )

      obligacionUpdatedFromDtoStub
    }

  }
}
