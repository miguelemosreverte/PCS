package spec.consumers.registrales.parametrica_recargo.unit_test

import akka.actor.ActorSystem
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents
import spec.consumers.registrales.parametrica_recargo.ParametricaRecargoProyectionistSpec
import spec.testsuite.ProjectionTestContext

class ParametricaRecargoProjectionUnitTest extends ParametricaRecargoProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[ParametricaRecargoEvents, ParametricaRecargoMessage.ParametricaRecargoMessageRoots] =
    new ParametricaRecargoProjectionUnitTestContext
}
