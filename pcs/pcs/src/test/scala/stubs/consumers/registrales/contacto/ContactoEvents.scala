package stubs.consumers.registrales.contacto

import consumers.registral.contacto.domain.ContactoEvents.ContactoUpdatedFromDto
import stubs.consumers.registrales.contacto.ContactoExternalDto.contactoStub

object ContactoEvents {

  def contactoUpdateFromDtoStub =
    ContactoUpdatedFromDto(
      aggregateRoot = contactoStub.CNC_ID,
      registro = contactoStub
    )

}
