package spec.consumers.registrales.plan_pago.unit_test

import akka.actor.ActorSystem
import consumers.registral.plan_pago.application.entities.PlanPagoMessage.PlanPagoMessageRoots
import consumers.registral.plan_pago.domain.PlanPagoEvents
import consumers.registral.plan_pago.domain.PlanPagoEvents.PlanPagoUpdatedFromDto
import consumers.registral.plan_pago.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class PlanPagoProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[PlanPagoEvents, PlanPagoMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: PlanPagoUpdatedFromDto =>
      (
        PlanPagoMessageRoots(
          e.sujetoId,
          e.objetoId,
          e.tipoObjeto,
          e.planPagoId
        ).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[PlanPagoEvents, PlanPagoMessageRoots] =
    new PlanPagoProjectionUnitTestKit(cassandraTestkit)
}
