package spec.consumers.registrales.parametrica_plan.unit_test

import akka.actor.ActorSystem
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanMessage
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents
import spec.consumers.registrales.parametrica_plan.ParametricaPlanProyectionistSpec
import spec.testsuite.ProjectionTestContext

class ParametricaPlanProjectionUnitTest extends ParametricaPlanProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[ParametricaPlanEvents, ParametricaPlanMessage.ParametricaPlanMessageRoots] =
    new ParametricaPlanProjectionUnitTestContext
}
