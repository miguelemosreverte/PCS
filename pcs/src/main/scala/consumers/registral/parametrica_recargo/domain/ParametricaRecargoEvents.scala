package consumers.registral.parametrica_recargo.domain

import java.time.LocalDateTime

import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto
import design_principles.actor_model.Event

sealed trait ParametricaRecargoEvents extends Event {
  def bprIndice: String
  def bprTipoIndice: String
  def bprFechaDesde: LocalDateTime
  def bprPeriodo: String
  def bprConcepto: String
  def bprImpuesto: String
}

object ParametricaRecargoEvents {
  case class ParametricaRecargoUpdatedFromDto(
      bprIndice: String,
      bprTipoIndice: String,
      bprFechaDesde: LocalDateTime,
      bprPeriodo: String,
      bprConcepto: String,
      bprImpuesto: String,
      registro: ParametricaRecargoExternalDto
  ) extends ParametricaRecargoEvents
}
