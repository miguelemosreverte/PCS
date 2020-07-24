package spec.consumers.registrales.tramite.unit_test

import akka.actor.ActorSystem
import consumers.registral.tramite.application.entities.TramiteMessage
import consumers.registral.tramite.domain.TramiteEvents
import spec.consumers.registrales.tramite.TramiteProyectionistSpec
import spec.testsuite.ProjectionTestContext

class TramiteProjectionUnitTest extends TramiteProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[TramiteEvents, TramiteMessage.TramiteMessageRoots] =
    new TramiteProjectionUnitTestContext
}
