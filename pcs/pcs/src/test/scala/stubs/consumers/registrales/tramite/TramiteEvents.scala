package stubs.consumers.registrales.tramite
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import stubs.consumers.registrales.tramite.TramiteExternalDto.tramiteStub

object TramiteEvents {
  def tramiteUpdatedFromDtoStub = TramiteUpdatedFromDto(
    sujetoId = tramiteStub.BTR_SUJ_IDENTIFICADOR,
    tramiteId = tramiteStub.BTR_TRMID,
    registro = tramiteStub
  )
}
