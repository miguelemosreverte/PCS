package consumers.no_registral.obligacion.domain

import java.time.LocalDateTime

import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.DetallesObligacion
import ddd._

case class ObligacionState(
    saldo: BigDecimal = 0,
    vencida: Boolean = false,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
    fechaVencimiento: Option[LocalDateTime] = None,
    exenta: Boolean = false,
    porcentajeExencion: Option[BigDecimal] = None,
    registro: Option[ObligacionExternalDto] = None,
    detallesObligacion: Seq[DetallesObligacion] = Seq.empty,
    juicioId: Option[BigInt] = None
) extends AbstractState[ObligacionEvents] {

  override def +(event: ObligacionEvents): ObligacionState =
    (event match {
      case e: ObligacionEvents.ObligacionAddedExencion =>
        copy(
          exenta = true,
          porcentajeExencion = e.exencion.BEX_PORCENTAJE
        )
      case e: ObligacionEvents.ObligacionRemoved =>
        empty
      case e: ObligacionEvents.ObligacionUpdatedFromDto =>
        copy(
          saldo = e.registro.BOB_SALDO,
          vencida = isVencida(e.registro),
          fechaVencimiento = e.registro.BOB_VENCIMIENTO,
          registro = Some(e.registro),
          detallesObligacion = e.detallesObligacion,
          juicioId = e.registro.BOB_JUI_ID
        )

      case _ => this
    }).copy(
      fechaUltMod = LocalDateTime.now
    )

  def empty = ObligacionState()

  private def isVencida(dto: ObligacionExternalDto): Boolean = {
    dto.BOB_VENCIMIENTO
      .exists { fechaVencimiento =>
        fechaVencimiento isBefore LocalDateTime.now
      }
  }

}
