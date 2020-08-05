package generators.consumers.no_registrales.sujeto

import java.util.concurrent.atomic.AtomicInteger

import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromTri
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoTri
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoUpdatedFromTri
import generators.consumers.no_registrales.{Generator, Helper}
import play.api.libs.json.Reads
import stubs.loadExample
import utils.generators.Model.deliveryId
import consumers.no_registral.sujeto.infrastructure.json._
import stubs.consumers.no_registrales.sujeto.SujetoExternalDto.sujetoTriStub

class SujetoTriGenerator extends Generator[SujetoTri] {
  import SujetoTriGenerator._

  lazy val example = loadExample[SujetoTri]("assets/examples/DGR-COP-SUJETO-TRI.json")

  def next: SujetoTri = {
    val id = i.incrementAndGet().toString
    example.copy(
      EV_ID = deliveryId,
      SUJ_IDENTIFICADOR = id
    )
  }

  override def toJson(e: SujetoTri): String = e.toJson

  override def aggregateRoot(e: SujetoTri): String = e.aggregateRoot
}

object SujetoTriGenerator {
  implicit class SujetoTriGeneratorHelper(sujetoTri: SujetoTri) extends Helper {

    def toJson: String =
      serialization.encode(sujetoTri)

    def toEvent: SujetoUpdatedFromTri =
      SujetoUpdatedFromTri(deliveryId, sujetoTriStub.SUJ_IDENTIFICADOR, sujetoTriStub)

    def toCommand: SujetoUpdateFromTri =
      SujetoUpdateFromTri(deliveryId, sujetoTriStub.SUJ_IDENTIFICADOR, sujetoTriStub)

  }
}
