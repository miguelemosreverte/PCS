package stubs.consumers.registrales.domicilio_sujeto

import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import stubs.consumers.registrales.domicilio_sujeto.DomicilioSujetoExternalDto.{
  domicilioSujetoAntStub,
  domicilioSujetoTriStub
}

object DomicilioSujetoEvents {
  def domicilioSujetoUpdatedFromDtoTriStub: DomicilioSujetoUpdatedFromDto = DomicilioSujetoUpdatedFromDto(
    sujetoId = domicilioSujetoTriStub.BDS_SUJ_IDENTIFICADOR,
    domicilioSujetoId = domicilioSujetoTriStub.BDS_DOM_ID,
    registro = domicilioSujetoTriStub
  )
  def domicilioSujetoUpdatedFromDtoAntStub: DomicilioSujetoUpdatedFromDto = DomicilioSujetoUpdatedFromDto(
    sujetoId = domicilioSujetoTriStub.BDS_SUJ_IDENTIFICADOR,
    domicilioSujetoId = domicilioSujetoTriStub.BDS_DOM_ID,
    registro = domicilioSujetoAntStub
  )
}
