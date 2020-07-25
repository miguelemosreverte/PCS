package stubs.consumers.no_registrales.obligacion

import consumers.no_registral.obligacion.domain.ObligacionEvents._
import stubs.consumers.no_registrales.obligacion.ObligacionCommands.detallesObligaciones
import stubs.consumers.no_registrales.obligacion.ObligacionExternalDtoStub._

object ObligacionEvents {
  def obligacionPersistedSnapshot = ObligacionPersistedSnapshot(
    obligacionUpdatedFromDtoTriStub.sujetoId,
    obligacionUpdatedFromDtoTriStub.objetoId,
    obligacionUpdatedFromDtoTriStub.tipoObjeto,
    obligacionUpdatedFromDtoTriStub.obligacionId,
    registro = Some(stubs.consumers.no_registrales.obligacion.ObligacionExternalDtoStub.obligacionesTri),
    exenta = false,
    porcentajeExencion = 0,
    vencida = false,
    saldo = 0
  )
  def obligacionUpdatedFromDtoTriStub =
    ObligacionUpdatedFromDto(
      obligacionesTri.BOB_SUJ_IDENTIFICADOR,
      obligacionesTri.BOB_SOJ_IDENTIFICADOR,
      obligacionesTri.BOB_SOJ_TIPO_OBJETO,
      obligacionesTri.BOB_OBN_ID,
      obligacionesTri,
      detallesObligaciones.getOrElse(Seq.empty)
    )
  def obligacionUpdatedFromDtoAntStub =
    ObligacionUpdatedFromDto(
      obligacionesTri.BOB_SUJ_IDENTIFICADOR,
      obligacionesTri.BOB_SOJ_IDENTIFICADOR,
      obligacionesTri.BOB_SOJ_TIPO_OBJETO,
      obligacionesTri.BOB_OBN_ID,
      obligacionesAnt,
      detallesObligaciones.getOrElse(Seq.empty)
    )
  def obligacionRemovedStub = ObligacionRemoved(
    obligacionesTri.BOB_SUJ_IDENTIFICADOR,
    obligacionesTri.BOB_SOJ_IDENTIFICADOR,
    obligacionesTri.BOB_SOJ_TIPO_OBJETO,
    obligacionesTri.BOB_OBN_ID
  )

}
