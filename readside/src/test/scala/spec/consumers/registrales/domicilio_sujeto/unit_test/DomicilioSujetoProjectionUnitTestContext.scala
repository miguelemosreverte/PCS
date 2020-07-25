package spec.consumers.registrales.domicilio_sujeto.unit_test

import akka.actor.ActorSystem
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage.DomicilioSujetoMessageRoots
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import consumers.registral.domicilio_sujeto.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class DomicilioSujetoProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[DomicilioSujetoEvents, DomicilioSujetoMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: DomicilioSujetoUpdatedFromDto =>
      (
        DomicilioSujetoMessageRoots(e.sujetoId, e.domicilioSujetoId).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[DomicilioSujetoEvents, DomicilioSujetoMessageRoots] =
    new DomicilioSujetoProjectionUnitTestKit(cassandraTestkit)
}
