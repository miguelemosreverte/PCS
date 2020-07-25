package stubs.consumers.registrales.subasta

import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import stubs.consumers.registrales.subasta.SubastaExternalDtoStub.subastaStub

object SubastaEvents {
  def subastaUpdatedFromDtoStub = SubastaUpdatedFromDto(
    sujetoId = subastaStub.BSB_SUJ_IDENTIFICADOR_ADQ,
    objetoId = subastaStub.BSB_SOJ_IDENTIFICADOR,
    tipoObjeto = subastaStub.BSB_SOJ_TIPO_OBJETO,
    subastaId = subastaStub.BSB_SUB_ID,
    registro = subastaStub
  )
}
