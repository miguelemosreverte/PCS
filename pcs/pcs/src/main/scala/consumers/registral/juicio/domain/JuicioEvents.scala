package consumers.registral.juicio.domain

import consumers.registral.juicio.application.entities.JuicioExternalDto
import consumers.registral.juicio.application.entities.JuicioExternalDto.DetallesJuicio
import design_principles.actor_model.Event

sealed trait JuicioEvents extends Event {
  def sujetoId: String
  def objetoId: String
  def tipoObjeto: String
  def juicioId: String
}

object JuicioEvents {
  case class JuicioUpdatedFromDto(
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      juicioId: String,
      registro: JuicioExternalDto,
      detallesJuicio: Seq[DetallesJuicio]
  ) extends JuicioEvents
}
