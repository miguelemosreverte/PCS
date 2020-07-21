package spec.consumers.no_registrales.objeto.unit_test

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.domain.ObjetoEvents
import spec.consumers.no_registrales.objeto.ObjetoProjectionSpec
import spec.testsuite.ProjectionTestContext

class ObjetoProjectionUnitTest extends ObjetoProjectionSpec {
  override def testContext()(implicit system: ActorSystem): ProjectionTestContext[ObjetoEvents, ObjetoMessageRoots] =
    new ObjetoProjectionUnitTestContext
}
