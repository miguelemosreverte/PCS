package spec.consumers.registrales.tramite.unit_test

import akka.actor.ActorSystem
import consumers.registral.tramite.application.entities.TramiteMessage.TramiteMessageRoots
import consumers.registral.tramite.domain.TramiteEvents
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import consumers.registral.tramite.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class TramiteProjectionUnitTestContext(implicit system: ActorSystem)
  extends ProjectionTestContext[TramiteEvents, TramiteMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: TramiteUpdatedFromDto =>
      (
        TramiteMessageRoots(
          e.sujetoId,
          e.tramiteId
        ).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[TramiteEvents, TramiteMessageRoots] =
    new TramiteProjectionUnitTestKit(cassandraTestkit)
}
