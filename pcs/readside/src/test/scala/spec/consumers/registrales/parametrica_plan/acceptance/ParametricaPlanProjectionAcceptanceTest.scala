package spec.consumers.registrales.parametrica_plan.acceptance

import akka.actor.ActorSystem
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanMessage
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents
import org.scalatest.Ignore
import spec.consumers.registrales.parametrica_plan.ParametricaPlanProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class ParametricaPlanProjectionAcceptanceTest extends ParametricaPlanProyectionistSpec {
  override def testContext()(implicit system: ActorSystem): ProjectionTestContext[ParametricaPlanEvents, ParametricaPlanMessage.ParametricaPlanMessageRoots] =
    new ParametricaPlanProjectionAcceptanceTestContext
}
