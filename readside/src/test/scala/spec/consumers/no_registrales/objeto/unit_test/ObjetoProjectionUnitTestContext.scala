package spec.consumers.no_registrales.objeto.unit_test

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class ObjetoProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[ObjetoEvents, ObjetoMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ObjetoSnapshotPersisted =>
      (
        ObjetoMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[ObjetoEvents, ObjetoMessageRoots] =
    new ObjetoProjectionUnitTestKit(cassandraTestkit)
}
