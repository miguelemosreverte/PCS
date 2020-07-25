package spec.consumers.registrales.subasta.acceptance

import akka.actor.ActorSystem
import consumers.registral.subasta.application.entities.SubastaMessage
import consumers.registral.subasta.domain.SubastaEvents
import org.scalatest.Ignore
import spec.consumers.registrales.subasta.SubastaProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class SubastaProjectionAcceptanceTest extends SubastaProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[SubastaEvents, SubastaMessage.SubastaMessageRoots] =
    new SubastaProjectionAcceptanceTestContext
}
