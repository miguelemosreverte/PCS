package registrales.exencion.testkit

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ExencionMessageRoot

trait ExencionImplicitConversions {

  implicit def toExencionMessageRoot: Exencion => ExencionMessageRoot =
    exencion =>
      ExencionMessageRoot(
        exencion.BEX_SUJ_IDENTIFICADOR,
        exencion.BEX_SOJ_IDENTIFICADOR,
        exencion.BEX_SOJ_TIPO_OBJETO,
        exencion.BEX_EXE_ID
      )

}
