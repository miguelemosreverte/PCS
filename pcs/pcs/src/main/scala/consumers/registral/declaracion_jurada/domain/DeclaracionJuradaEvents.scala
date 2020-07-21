package consumers.registral.declaracion_jurada.domain

import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaExternalDto
import design_principles.actor_model.Event

sealed trait DeclaracionJuradaEvents extends Event {
  def sujetoId: String
  def objetoId: String
  def tipoObjeto: String
  def declaracionJuradaId: String
}

object DeclaracionJuradaEvents {
  case class DeclaracionJuradaUpdatedFromDto(
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      declaracionJuradaId: String,
      registro: DeclaracionJuradaExternalDto
  ) extends DeclaracionJuradaEvents

}
