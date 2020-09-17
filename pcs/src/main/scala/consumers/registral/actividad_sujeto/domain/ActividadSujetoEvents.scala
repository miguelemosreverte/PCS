package consumers.registral.actividad_sujeto.domain
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoExternalDto
import design_principles.actor_model.Event

sealed trait ActividadSujetoEvents extends Event {
  def sujetoId: String
  def actividadSujetoId: String
}

object ActividadSujetoEvents {

  case class ActividadSujetoUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      actividadSujetoId: String,
      registro: ActividadSujetoExternalDto
  ) extends ActividadSujetoEvents
}
