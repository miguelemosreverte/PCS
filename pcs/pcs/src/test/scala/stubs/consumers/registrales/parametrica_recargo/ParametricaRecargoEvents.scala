package stubs.consumers.registrales.parametrica_recargo

import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto
import stubs.consumers.registrales.parametrica_recargo.ParametricaRecargoExternalDto.{
  parametricaRecargoAntStub,
  parametricaRecargoTriStub
}

object ParametricaRecargoEvents {
  def parametricaPlanUpdatedFromDtoAntStub = ParametricaRecargoUpdatedFromDto(
    bprIndice = parametricaRecargoAntStub.BPR_INDICE,
    bprTipoIndice = parametricaRecargoAntStub.BPR_TIPO_INDICE,
    bprFechaDesde = parametricaRecargoAntStub.BPR_FECHA_DESDE,
    bprPeriodo = parametricaRecargoAntStub.BPR_PERIODO,
    bprConcepto = parametricaRecargoAntStub.BPR_CONCEPTO,
    bprImpuesto = parametricaRecargoAntStub.BPR_IMPUESTO,
    registro = parametricaRecargoAntStub
  )
  def parametricaPlanUpdatedFromDtoTriStub = ParametricaRecargoUpdatedFromDto(
    bprIndice = parametricaRecargoTriStub.BPR_INDICE,
    bprTipoIndice = parametricaRecargoTriStub.BPR_TIPO_INDICE,
    bprFechaDesde = parametricaRecargoTriStub.BPR_FECHA_DESDE,
    bprPeriodo = parametricaRecargoTriStub.BPR_PERIODO,
    bprConcepto = parametricaRecargoTriStub.BPR_CONCEPTO,
    bprImpuesto = parametricaRecargoTriStub.BPR_IMPUESTO,
    registro = parametricaRecargoTriStub
  )
}
