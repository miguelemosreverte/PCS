package spec.consumers.registrales.tramite.acceptance

import akka.actor.ActorSystem
import consumers.registral.tramite.application.entities.TramiteMessage
import consumers.registral.tramite.domain.TramiteEvents
import org.scalatest.Ignore
import spec.consumers.registrales.tramite.TramiteProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class TramiteProjectionAcceptanceTest extends TramiteProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[TramiteEvents, TramiteMessage.TramiteMessageRoots] =
    new TramiteProjectionAcceptanceTestContext
}
