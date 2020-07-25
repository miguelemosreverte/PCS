package spec.consumers.no_registrales.obligacion.unit_test

import akka.actor.ActorSystem
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import design_principles.projection.mock.CassandraTestkitMock
import spec.testsuite.ProjectionTestContext
import consumers.no_registral.obligacion.infrastructure.json._
import spec.testkit.ProjectionTestkit

class ObligacionProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[ObligacionEvents, ObligacionMessageRoots] {

  val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ObligacionPersistedSnapshot =>
      (
        ObligacionMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto, e.obligacionId).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[ObligacionEvents, ObligacionMessageRoots] =
    new ObligacionProjectionUnitTestKit(cassandraTestkit)
}
