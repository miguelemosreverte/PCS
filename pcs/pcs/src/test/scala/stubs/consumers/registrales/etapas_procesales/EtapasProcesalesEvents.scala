package stubs.consumers.registrales.etapas_procesales

import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents.EtapasProcesalesUpdatedFromDto
import stubs.consumers.registrales.etapas_procesales.EtapasProcesalesExternalDto.{
  etapasProcesalesAntStub,
  etapasProcesalesTriStub
}

object EtapasProcesalesEvents {
  def etapasProcesalesUpdatedFromDtoAntStub = EtapasProcesalesUpdatedFromDto(
    juicioId = etapasProcesalesAntStub.BEP_JUI_ID,
    etapaId = etapasProcesalesAntStub.BPE_ETA_ID,
    registro = etapasProcesalesAntStub
  )
  def etapasProcesalesUpdatedFromDtoTriStub = EtapasProcesalesUpdatedFromDto(
    juicioId = etapasProcesalesAntStub.BEP_JUI_ID,
    etapaId = etapasProcesalesAntStub.BPE_ETA_ID,
    registro = etapasProcesalesTriStub
  )
}
