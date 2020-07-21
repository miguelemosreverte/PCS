package spec.consumers.no_registrales.sujeto.unit_test

import akka.actor.ActorSystem
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoSnapshotPersisted
import consumers.no_registral.sujeto.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class SujetoProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[SujetoEvents, SujetoMessageRoots] {

  val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: SujetoSnapshotPersisted =>
      (
        SujetoMessageRoots(e.sujetoId).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[SujetoEvents, SujetoMessageRoots] =
    new SujetoProjectionUnitTestKit(cassandraTestkit)
}
