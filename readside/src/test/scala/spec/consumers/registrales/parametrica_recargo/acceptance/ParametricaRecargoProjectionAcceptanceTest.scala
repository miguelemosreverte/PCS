package spec.consumers.registrales.parametrica_recargo.acceptance

import akka.actor.ActorSystem
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents
import org.scalatest.Ignore
import spec.consumers.registrales.parametrica_recargo.ParametricaRecargoProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class ParametricaRecargoProjectionAcceptanceTest extends ParametricaRecargoProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[ParametricaRecargoEvents, ParametricaRecargoMessage.ParametricaRecargoMessageRoots] =
    new ParametricaRecargoProjectionAcceptanceTestContext
}