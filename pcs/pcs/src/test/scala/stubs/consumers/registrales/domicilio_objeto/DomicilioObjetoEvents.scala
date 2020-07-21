package stubs.consumers.registrales.domicilio_objeto

import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents.DomicilioObjetoUpdatedFromDto
import stubs.consumers.registrales.domicilio_objeto.DomicilioObjetoExternalDto.{
  domicilioObjetoAntStub,
  domicilioObjetoTriStub
}

object DomicilioObjetoEvents {
  def domicilioObjetoUpdatedFromDtoTriStub = DomicilioObjetoUpdatedFromDto(
    sujetoId = domicilioObjetoTriStub.BDO_SUJ_IDENTIFICADOR,
    objetoId = domicilioObjetoTriStub.BDO_SOJ_IDENTIFICADOR,
    tipoObjeto = domicilioObjetoTriStub.BDO_SOJ_TIPO_OBJETO,
    domicilioObjetoId = domicilioObjetoTriStub.BDO_DOM_ID,
    registro = domicilioObjetoTriStub
  )
  def domicilioObjetoUpdatedFromDtoAntStub = DomicilioObjetoUpdatedFromDto(
    sujetoId = domicilioObjetoAntStub.BDO_SUJ_IDENTIFICADOR,
    objetoId = domicilioObjetoAntStub.BDO_SOJ_IDENTIFICADOR,
    tipoObjeto = domicilioObjetoAntStub.BDO_SOJ_TIPO_OBJETO,
    domicilioObjetoId = domicilioObjetoTriStub.BDO_DOM_ID,
    registro = domicilioObjetoAntStub
  )
}
