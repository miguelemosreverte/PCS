package generators.consumers.no_registrales.sujeto

import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromAnt
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoAnt
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoUpdatedFromAnt
import consumers.no_registral.sujeto.infrastructure.json._
import generators.consumers.no_registrales.{Generator, Helper}
import stubs.consumers.no_registrales.sujeto.SujetoExternalDto.sujetoAntStub
import stubs.loadExample
import utils.generators.Model.deliveryId

class SujetoAntGenerator extends Generator[SujetoAnt] {
  import SujetoAntGenerator._
  lazy val example = loadExample[SujetoAnt]("assets/examples/DGR-COP-SUJETO-ANT.json")

  def next(id: Int): SujetoAnt =
    example.copy(
      EV_ID = id,
      SUJ_IDENTIFICADOR = id.toString
    )

  override def toJson(e: SujetoAnt): String = e.toJson

  override def aggregateRoot(e: SujetoAnt): String = e.aggregateRoot
}

object SujetoAntGenerator {
  implicit class SujetoAntGeneratorHelper(sujetoAnt: SujetoAnt) extends Helper {

    def toJson: String =
      serialization.encode(sujetoAnt)

    def toEvent: SujetoUpdatedFromAnt =
      SujetoUpdatedFromAnt(deliveryId, sujetoAntStub.SUJ_IDENTIFICADOR, sujetoAntStub)

    def toCommand: SujetoUpdateFromAnt =
      SujetoUpdateFromAnt(deliveryId, sujetoAntStub.SUJ_IDENTIFICADOR, sujetoAntStub)

  }
}
