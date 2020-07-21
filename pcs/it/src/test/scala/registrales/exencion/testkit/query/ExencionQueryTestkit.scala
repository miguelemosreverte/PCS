package registrales.exencion.testkit.query

import consumers.no_registral.objeto.application.entities.ObjetoMessage.ExencionMessageRoot
import consumers.no_registral.objeto.application.entities.ObjetoResponses.GetExencionResponse
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryTestKit
import design_principles.actor_model.testkit.QueryTestkit

trait ExencionQueryTestkit extends QueryTestkit with NoRegistralesQueryTestKit {
  def getStateExencion(exencionMessageRoot: ExencionMessageRoot): GetExencionResponse
}
