package consumers.no_registral.objeto.domain

import java.time.LocalDateTime

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import ddd.AbstractState

case class ObjetoState(
    saldo: BigDecimal = 0,
    obligacionesSaldo: Map[String, BigDecimal] = Map.empty,
    obligacionesVencidasSaldo: Map[String, BigDecimal] = Map.empty,
    obligaciones: Set[String] = Set.empty,
    sujetos: Set[String] = Set.empty,
    sujetoResponsable: Option[String] = None,
    vencimiento: Boolean = false,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
    registro: Option[ObjetoExternalDto] = None,
    tags: Set[String] = Set.empty,
    isResponsable: Boolean = false,
    lastDeliveryIdByEvents: Map[String, BigInt] = Map.empty,
    porcentajeResponsabilidad: BigDecimal = 0,
    exenciones: Set[Exencion] = Set.empty,
    isBaja: Boolean = false
) extends AbstractState[ObjetoEvents] {

  override def +(event: ObjetoEvents): ObjetoState =
    changeState(event).copy(
      fechaUltMod = LocalDateTime.now,
      lastDeliveryIdByEvents = lastDeliveryIdByEvents + ((event.getClass.getSimpleName, event.deliveryId))
    )

  private def changeState(event: ObjetoEvents): ObjetoState =
    event match {
      case cmd: ObjetoEvents.ObjetoUpdatedCotitulares =>
        copy(
          sujetos = cmd.cotitulares
        )
      case ObjetoEvents.ObjetoAddedExencion(deliveryId, sujetoId, objetoId, tipoObjeto, exencion) =>
        copy(
          exenciones = exenciones + exencion
        )
      case evt: ObjetoEvents.ObjetoUpdatedFromTri =>
        copy(
          sujetoResponsable = evt.sujetoResponsable match {
            case Some(value) => Some(value)
            case None => this.sujetoResponsable
          },
          isResponsable = evt.isResponsable.getOrElse(false),
          registro = Some(evt.registro),
          sujetos = sujetos + evt.sujetoId
        )
      case evt: ObjetoEvents.ObjetoUpdatedFromAnt =>
        copy(
          registro = Some(evt.registro),
          sujetos = sujetos + evt.sujetoId
        )
      case evt: ObjetoEvents.ObjetoUpdatedFromObligacionBajaSet =>
        val obligacionesSaldo_ = obligacionesSaldo - (evt.obligacionId)
        copy(
          saldo = obligacionesSaldo_.values.sum,
          obligaciones = obligaciones - evt.obligacionId,
          obligacionesSaldo = obligacionesSaldo_
        )
      case evt: ObjetoEvents.ObjetoUpdatedFromObligacion =>
        val _obligacionesVencidasSaldo =
          if (evt.obligacionVencida) {
            obligacionesVencidasSaldo + (evt.obligacionId -> evt.saldoObligacion)
          } else {
            obligacionesVencidasSaldo + (evt.obligacionId -> BigDecimal(0))
          }
        val obligacionesSaldo_ = obligacionesSaldo + (evt.obligacionId -> evt.saldoObligacion)
        copy(
          saldo = obligacionesSaldo_.values.sum,
          obligaciones = obligaciones + evt.obligacionId,
          obligacionesSaldo = obligacionesSaldo_,
          sujetos = sujetos + evt.sujetoId,
          vencimiento = vencimiento || evt.obligacionVencida,
          obligacionesVencidasSaldo = _obligacionesVencidasSaldo
        )
      case evt: ObjetoEvents.ObjetoSnapshotPersisted =>
        copy(
          saldo = evt.saldo,
          sujetos = evt.cotitulares,
          vencimiento = evt.vencimiento,
          sujetoResponsable = Some(evt.sujetoResponsable),
          obligacionesSaldo = evt.obligacionesSaldo,
          obligacionesVencidasSaldo = evt.obligacionesVencidasSaldo,
          tags = evt.tags
        )

      case evt: ObjetoEvents.ObjetoTagAdded =>
        copy(tags = tags + evt.tagAdded)
      case evt: ObjetoEvents.ObjetoTagRemoved =>
        copy(tags = tags - evt.tagRemoved)

      case evt: ObjetoEvents.ObjetoBajaSet =>
        copy(
          sujetoResponsable = evt.sujetoResponsable match {
            case Some(value) => Some(value)
            case None => this.sujetoResponsable
          },
          isResponsable = evt.isResponsable.getOrElse(false),
          registro = Some(evt.registro),
          isBaja = true
        )

      case evt =>
        log.warn(s"Unexpected event at ObjetoState ${evt}")
        this
    }
}
