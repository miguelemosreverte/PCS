package spec.consumers.registrales.plan_pago.acceptance

import akka.actor.ActorSystem
import consumers.registral.plan_pago.application.entities.PlanPagoMessage.PlanPagoMessageRoots
import consumers.registral.plan_pago.domain.PlanPagoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class PlanPagoProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[PlanPagoEvents, PlanPagoMessageRoots] {

  import system.dispatcher
  truncateTables(
    Seq(
      "buc_planes_pago"
    )
  )
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[PlanPagoEvents, PlanPagoMessageRoots] =
    new PlanPagoProjectionAcceptanceTestKit(cassandraTestkit)
}
