package spec.consumers.registrales.plan_pago.acceptance

import akka.actor.ActorSystem
import consumers.registral.plan_pago.application.entities.PlanPagoMessage
import consumers.registral.plan_pago.domain.PlanPagoEvents
import org.scalatest.Ignore
import spec.consumers.registrales.plan_pago.PlanPagoProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class PlanPagoProjectionAcceptanceTest extends PlanPagoProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[PlanPagoEvents, PlanPagoMessage.PlanPagoMessageRoots] =
    new PlanPagoProjectionAcceptanceTestContext
}
