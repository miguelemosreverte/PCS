package consumers.no_registral.cotitularidad.domain

import java.time.LocalDateTime

import ddd.AbstractState

case class CotitularidadState(
    sujetoResponsable: String = "",
    sujetosCotitulares: Set[String] = Set.empty,
    lastDeliveryIdByEvents: Map[String, BigInt] = Map.empty,
    fechaUltMod: LocalDateTime = LocalDateTime.now
) extends AbstractState[CotitularidadEvents] {
  override def +(event: CotitularidadEvents): CotitularidadState =
    event match {
      case evt: CotitularidadEvents.CotitularidadAddedSujetoCotitular =>
        copy(
          sujetoResponsable = if (evt.isResponsable.getOrElse(false)) evt.sujetoId else sujetoResponsable,
          sujetosCotitulares = sujetosCotitulares + evt.sujetoId,
          lastDeliveryIdByEvents = lastDeliveryIdByEvents + ((evt.getClass.getSimpleName, evt.deliveryId)),
          fechaUltMod = LocalDateTime.now
        )
      case _ => this
    }
}
