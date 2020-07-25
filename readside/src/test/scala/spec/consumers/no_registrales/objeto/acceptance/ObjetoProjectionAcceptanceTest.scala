package spec.consumers.no_registrales.objeto.acceptance

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.objeto.domain.ObjetoEvents
import org.scalatest.Ignore
import spec.consumers.no_registrales.objeto.ObjetoProjectionSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class ObjetoProjectionAcceptanceTest extends ObjetoProjectionSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[ObjetoEvents, ObjetoMessage.ObjetoMessageRoots] =
    new ObjetoProjectionAcceptanceTestContext
}
