package spec.consumers.no_registrales.obligacion.acceptance

import akka.actor.ActorSystem
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain.ObligacionEvents
import org.scalatest.Ignore
import spec.consumers.no_registrales.obligacion.ObligacionProjectionSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class ObligacionProjectionAcceptanceTest extends ObligacionProjectionSpec {

  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[ObligacionEvents, ObligacionMessageRoots] =
    new ObligacionProjectionAcceptanceTestContext
}
