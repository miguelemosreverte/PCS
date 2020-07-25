package consumers.no_registral.obligacion.application.entities

import java.time.LocalDateTime

import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.DetallesObligacion
import design_principles.actor_model.Response

sealed trait ObligacionResponses
object ObligacionResponses {

  case class GetObligacionResponse(
      saldo: BigDecimal = 0,
      vencida: Boolean = false,
      fechaUltMod: LocalDateTime = LocalDateTime.MIN,
      registro: Option[ObligacionExternalDto] = None,
      detallesObligacion: Seq[DetallesObligacion] = Seq.empty,
      exenta: Boolean = false,
      porcentajeExencion: BigDecimal = 0,
      fechaVencimiento: Option[LocalDateTime] = Some(LocalDateTime.MAX),
      juicioId: Option[BigInt] = None
  ) extends Response
}
