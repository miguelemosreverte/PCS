package spec.consumers.registrales.subasta.unit_test

import akka.actor.ActorSystem
import consumers.registral.subasta.application.entities.SubastaMessage
import consumers.registral.subasta.domain.SubastaEvents
import spec.consumers.registrales.subasta.SubastaProyectionistSpec
import spec.testsuite.ProjectionTestContext

class SubastaProjectionUnitTest extends SubastaProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[SubastaEvents, SubastaMessage.SubastaMessageRoots] =
    new SubastaProjectionUnitTestContext
}
