package spec.consumers.registrales.exencion.unit_test

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ExencionMessageRoot
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import design_principles.projection.mock.CassandraTestkitMock
import spec.testsuite.ProjectionTestContext
import consumers.no_registral.objeto.infrastructure.json._
import spec.testkit.ProjectionTestkit

class ExencionProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[ObjetoAddedExencion, ExencionMessageRoot] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ObjetoEvents.ObjetoAddedExencion =>
      (
        ExencionMessageRoot(
          e.sujetoId,
          e.objetoId,
          e.tipoObjeto,
          e.exencion.BEX_EXE_ID
        ).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[ObjetoAddedExencion, ExencionMessageRoot] =
    new ExencionProjectionUnitTestKit(cassandraTestkit)
}
