package stubs.consumers.registrales.calendario

import consumers.registral.calendario.domain.CalendarioEvents.CalendarioUpdatedFromDto
import stubs.consumers.registrales.calendario.CalendarioExternalDtoStub.calendarioStub

object CalendarioEvents {
  def calendarioUpdatedFromDtoStub = CalendarioUpdatedFromDto(
    aggregateRoot = calendarioStub.BCL_IDENTIFICADOR,
    registro = calendarioStub
  )
}
