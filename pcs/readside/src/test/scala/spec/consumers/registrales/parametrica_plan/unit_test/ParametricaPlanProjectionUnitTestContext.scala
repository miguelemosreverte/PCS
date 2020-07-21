package spec.consumers.registrales.parametrica_plan.unit_test

import akka.actor.ActorSystem
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanMessage.ParametricaPlanMessageRoots
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents.ParametricaPlanUpdatedFromDto
import consumers.registral.parametrica_plan.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class ParametricaPlanProjectionUnitTestContext(implicit system: ActorSystem)
  extends ProjectionTestContext[ParametricaPlanEvents, ParametricaPlanMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ParametricaPlanUpdatedFromDto =>
      (
        ParametricaPlanMessageRoots(e.bppFpmId).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[ParametricaPlanEvents, ParametricaPlanMessageRoots] =
    new ParametricaPlanProjectionUnitTestKit(cassandraTestkit)
}
