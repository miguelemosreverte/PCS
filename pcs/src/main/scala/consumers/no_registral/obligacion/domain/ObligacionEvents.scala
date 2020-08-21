package consumers.no_registral.obligacion.domain

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.DetallesObligacion
import design_principles.actor_model.Event

sealed trait ObligacionEvents extends Event {
  def sujetoId: String
  def objetoId: String
  def tipoObjeto: String
  def obligacionId: String
}

object ObligacionEvents {

  case class ObligacionPersistedSnapshot(
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      registro: Option[ObligacionExternalDto],
      exenta: Boolean,
      porcentajeExencion: BigDecimal,
      saldo: BigDecimal
  ) extends ObligacionEvents

  case class ObligacionUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      registro: ObligacionExternalDto,
      detallesObligacion: Seq[DetallesObligacion]
  ) extends ObligacionEvents

  case class ObligacionRemoved(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String
  ) extends ObligacionEvents

  case class ObligacionAddedExencion(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      exencion: Exencion
  ) extends ObligacionEvents

}
