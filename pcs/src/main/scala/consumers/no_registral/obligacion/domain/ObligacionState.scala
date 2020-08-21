package consumers.no_registral.obligacion.domain

import java.time.LocalDateTime

import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.DetallesObligacion
import ddd._

case class ObligacionState(
    saldo: BigDecimal = 0,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
    exenta: Boolean = false,
    porcentajeExencion: Option[BigDecimal] = None,
    registro: Option[ObligacionExternalDto] = None,
    lastDeliveryIdByEvents: Map[String, BigInt] = Map.empty,
    detallesObligacion: Seq[DetallesObligacion] = Seq.empty,
    juicioId: Option[BigInt] = None
) extends AbstractState[ObligacionEvents] {

  override def +(event: ObligacionEvents): ObligacionState =
    changeState(event).copy(fechaUltMod = LocalDateTime.now)

  private def changeState(event: ObligacionEvents): ObligacionState =
    event match {
      case e: ObligacionEvents.ObligacionAddedExencion =>
        copy(
          exenta = true,
          porcentajeExencion = e.exencion.BEX_PORCENTAJE,
          lastDeliveryIdByEvents = lastDeliveryIdByEvents + ((event.getClass.getSimpleName, e.deliveryId))
        )
      case e: ObligacionEvents.ObligacionRemoved =>
        empty
      case e: ObligacionEvents.ObligacionUpdatedFromDto =>
        copy(
          saldo = e.registro.BOB_SALDO,
          registro = Some(e.registro),
          detallesObligacion = e.detallesObligacion,
          juicioId = e.registro.BOB_JUI_ID,
          lastDeliveryIdByEvents = lastDeliveryIdByEvents + ((event.getClass.getSimpleName, e.deliveryId))
        )
      case _ => this
    }

  def empty = ObligacionState()
 }
