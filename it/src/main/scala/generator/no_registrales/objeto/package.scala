package no_registrales

import java.util.concurrent.atomic.AtomicInteger

import stubs.consumers.no_registrales.objeto.ObjetoExternalDto.{objetoAntStub, objetoTriStub}
import utils.generators.Model.deliveryId

package object objeto {

  def nextObjetoAntStub(i: AtomicInteger) = {
    val id = i.incrementAndGet().toString
    objetoAntStub.copy(
      EV_ID = deliveryId,
      SOJ_SUJ_IDENTIFICADOR = id,
      SOJ_IDENTIFICADOR = id
    )
  }

  def nextObjetoTriStub(i: AtomicInteger) = {
    val id = i.incrementAndGet().toString
    objetoTriStub.copy(
      EV_ID = deliveryId,
      SOJ_SUJ_IDENTIFICADOR = id,
      SOJ_IDENTIFICADOR = id
    )
  }

}
