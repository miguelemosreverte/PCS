package consumers.registral.plan_pago.domain

import consumers.registral.plan_pago.application.entities.PlanPagoExternalDto
import design_principles.actor_model.Event

sealed trait PlanPagoEvents extends Event {
  def sujetoId: String
  def objetoId: String
  def tipoObjeto: String
  def planPagoId: String
}

object PlanPagoEvents {
  case class PlanPagoUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      planPagoId: String,
      registro: PlanPagoExternalDto
  ) extends PlanPagoEvents
}
