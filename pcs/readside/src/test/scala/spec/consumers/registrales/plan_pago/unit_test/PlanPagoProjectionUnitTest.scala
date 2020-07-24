package spec.consumers.registrales.plan_pago.unit_test

import akka.actor.ActorSystem
import consumers.registral.plan_pago.application.entities.PlanPagoMessage
import consumers.registral.plan_pago.domain.PlanPagoEvents
import spec.consumers.registrales.plan_pago.PlanPagoProyectionistSpec
import spec.testsuite.ProjectionTestContext

class PlanPagoProjectionUnitTest extends PlanPagoProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[PlanPagoEvents, PlanPagoMessage.PlanPagoMessageRoots] =
    new PlanPagoProjectionUnitTestContext
}
