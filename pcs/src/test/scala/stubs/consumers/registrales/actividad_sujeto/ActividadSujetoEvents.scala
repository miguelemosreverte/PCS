package stubs.consumers.registrales.actividad_sujeto

import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import stubs.consumers.registrales.actividad_sujeto.ActividadSujetoExternalDto.actividadSujetoStub

object ActividadSujetoEvents {
  def actividadSujetoUpdatedFromDtoStub = ActividadSujetoUpdatedFromDto(
    sujetoId = actividadSujetoStub.BAT_SUJ_IDENTIFICADOR,
    actividadSujetoId = actividadSujetoStub.BAT_ATD_ID,
    registro = actividadSujetoStub
  )
}
