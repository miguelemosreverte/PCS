package spec.consumers.registrales.juicio.unit_test

import akka.actor.ActorSystem
import consumers.registral.juicio.application.entities.JuicioMessage.JuicioMessageRoots
import consumers.registral.juicio.domain.JuicioEvents
import consumers.registral.juicio.domain.JuicioEvents.JuicioUpdatedFromDto
import consumers.registral.juicio.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class JuicioProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[JuicioEvents, JuicioMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: JuicioUpdatedFromDto =>
      (
        JuicioMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto, e.juicioId).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[JuicioEvents, JuicioMessageRoots] =
    new JuicioProjectionUnitTestKit(cassandraTestkit)
}
