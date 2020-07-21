package spec.consumers.registrales.parametrica_recargo.unit_test

import akka.actor.ActorSystem
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage.ParametricaRecargoMessageRoots
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto
import consumers.registral.parametrica_recargo.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class ParametricaRecargoProjectionUnitTestContext(implicit system: ActorSystem)
  extends ProjectionTestContext[ParametricaRecargoEvents, ParametricaRecargoMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ParametricaRecargoUpdatedFromDto =>
      (
        ParametricaRecargoMessageRoots(e.bprIndice).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[ParametricaRecargoEvents, ParametricaRecargoMessageRoots] =
    new ParametricaRecargoProjectionUnitTestKit(cassandraTestkit)
}
