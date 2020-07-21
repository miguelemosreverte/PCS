package consumers.no_registral.sujeto.domain

import java.time.LocalDateTime

import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import ddd.AbstractState

final case class SujetoState(
    saldo: BigDecimal = 0,
    saldoObjetos: Map[String, BigDecimal] = Map.empty,
    saldoObjetosVencidos: Map[String, BigDecimal] = Map.empty,
    objetos: Set[(String, String)] = Set.empty,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
    registro: Option[SujetoExternalDto] = None,
    lastDeliveryIdByEvents: Map[String, BigInt] = Map.empty
) extends AbstractState[SujetoEvents] {
  def +(event: SujetoEvents): SujetoState =
    changeState(event).copy(
      fechaUltMod = LocalDateTime.now,
      lastDeliveryIdByEvents = lastDeliveryIdByEvents + ((event.getClass.getSimpleName, event.deliveryId))
    )

  private def changeState(event: SujetoEvents): SujetoState =
    event match {
      case SujetoEvents.SujetoUpdatedFromTri(_, _, registro) =>
        copy(
          registro = Some(registro)
        )
      case SujetoEvents.SujetoUpdatedFromAnt(_, _, registro) =>
        copy(
          registro = Some(registro)
        )
      case SujetoEvents.SujetoUpdatedFromObjeto(_, _, objetoId, tipoObjeto, saldoObjeto, saldoObligacionesVencidas) =>
        val objetoKey = s"$objetoId|$tipoObjeto"
        val _saldoObjetos = saldoObjetos + (objetoKey -> saldoObjeto)
        copy(
          objetos = objetos + ((objetoId, tipoObjeto)),
          saldoObjetos = _saldoObjetos,
          saldo = _saldoObjetos.values.sum,
          saldoObjetosVencidos = saldoObjetosVencidos + (objetoKey -> saldoObligacionesVencidas)
        )
      case SujetoEvents.SujetoBajaFromObjetoSet(_, _, objetoId, tipoObjeto) =>
        val objetoKey = s"$objetoId|$tipoObjeto"
        val _saldoObjetos = saldoObjetos - objetoKey
        copy(
          objetos = objetos - ((objetoId, tipoObjeto)),
          saldoObjetos = _saldoObjetos,
          saldo = _saldoObjetos.values.sum,
          saldoObjetosVencidos = saldoObjetosVencidos - objetoKey
        )
      case evt: SujetoEvents.SujetoSnapshotPersisted =>
        copy(
          saldo = evt.saldo
        )
      case _ => this
    }
}
