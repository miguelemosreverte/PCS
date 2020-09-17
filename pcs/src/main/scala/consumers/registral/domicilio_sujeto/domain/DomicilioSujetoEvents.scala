package consumers.registral.domicilio_sujeto.domain

import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoExternalDto
import design_principles.actor_model.Event

sealed trait DomicilioSujetoEvents extends Event {
  def sujetoId: String
  def domicilioSujetoId: String
}

object DomicilioSujetoEvents {
  case class DomicilioSujetoUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      domicilioSujetoId: String,
      registro: DomicilioSujetoExternalDto
  ) extends DomicilioSujetoEvents
}
