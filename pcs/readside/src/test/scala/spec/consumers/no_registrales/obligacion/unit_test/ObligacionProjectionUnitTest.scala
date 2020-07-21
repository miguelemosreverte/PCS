package spec.consumers.no_registrales.obligacion.unit_test

import akka.actor.ActorSystem
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain.ObligacionEvents
import spec.consumers.no_registrales.obligacion.ObligacionProjectionSpec
import spec.testsuite.ProjectionTestContext

class ObligacionProjectionUnitTest extends ObligacionProjectionSpec {

  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[ObligacionEvents, ObligacionMessageRoots] =
    new ObligacionProjectionUnitTestContext
}
