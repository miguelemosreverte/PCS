package consumers.registral.tramite.domain

import consumers.registral.tramite.application.entities.TramiteExternalDto.Tramite
import design_principles.actor_model.Event

sealed trait TramiteEvents extends Event {
  def sujetoId: String
  def tramiteId: String
}

object TramiteEvents {
  case class TramiteUpdatedFromDto(
      sujetoId: String,
      tramiteId: String,
      registro: Tramite
  ) extends TramiteEvents
}
