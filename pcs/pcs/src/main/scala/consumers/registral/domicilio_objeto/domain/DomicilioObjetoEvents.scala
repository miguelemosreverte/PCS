package consumers.registral.domicilio_objeto.domain

import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoExternalDto
import design_principles.actor_model.Event

sealed trait DomicilioObjetoEvents extends Event {
  def sujetoId: String
  def objetoId: String
  def tipoObjeto: String
  def domicilioObjetoId: String
}

object DomicilioObjetoEvents {
  case class DomicilioObjetoUpdatedFromDto(
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      domicilioObjetoId: String,
      registro: DomicilioObjetoExternalDto
  ) extends DomicilioObjetoEvents

}
