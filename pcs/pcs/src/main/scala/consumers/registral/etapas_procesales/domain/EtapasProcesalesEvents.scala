package consumers.registral.etapas_procesales.domain

import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesExternalDto
import design_principles.actor_model.Event

sealed trait EtapasProcesalesEvents extends Event {
  def juicioId: String
  def etapaId: String
}

object EtapasProcesalesEvents {
  case class EtapasProcesalesUpdatedFromDto(
      juicioId: String,
      etapaId: String,
      registro: EtapasProcesalesExternalDto
  ) extends EtapasProcesalesEvents

}
