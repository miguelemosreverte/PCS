package spec.consumers.registrales.subasta.unit_test

import akka.actor.ActorSystem
import consumers.registral.subasta.application.entities.SubastaMessage.SubastaMessageRoots
import consumers.registral.subasta.domain.SubastaEvents
import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import consumers.registral.subasta.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class SubastaProjectionUnitTestContext(implicit system: ActorSystem)
  extends ProjectionTestContext[SubastaEvents, SubastaMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: SubastaUpdatedFromDto =>
      (
        SubastaMessageRoots(
          e.sujetoId,
          e.objetoId,
          e.tipoObjeto,
          e.subastaId
        ).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[SubastaEvents, SubastaMessageRoots] =
    new SubastaProjectionUnitTestKit(cassandraTestkit)
}
