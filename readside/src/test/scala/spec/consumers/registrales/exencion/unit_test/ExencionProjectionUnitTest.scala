package spec.consumers.registrales.exencion.unit_test

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.objeto.domain.ObjetoEvents
import spec.consumers.registrales.exencion.ExencionProyectionistSpec
import spec.testsuite.ProjectionTestContext

class ExencionProjectionUnitTest extends ExencionProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[ObjetoEvents.ObjetoAddedExencion, ObjetoMessage.ExencionMessageRoot] =
    new ExencionProjectionUnitTestContext
}
