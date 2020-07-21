package spec.consumers.registrales.actividad_sujeto.unit_test

import akka.actor.ActorSystem
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage.ActividadSujetoMessageRoots
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import consumers.registral.actividad_sujeto.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class ActividadSujetoProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[ActividadSujetoEvents, ActividadSujetoMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ActividadSujetoUpdatedFromDto =>
      (
        ActividadSujetoMessageRoots(e.sujetoId, e.actividadSujetoId).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[ActividadSujetoEvents, ActividadSujetoMessageRoots] =
    new ActividadSujetoProjectionUnitTestKit(cassandraTestkit)
}
