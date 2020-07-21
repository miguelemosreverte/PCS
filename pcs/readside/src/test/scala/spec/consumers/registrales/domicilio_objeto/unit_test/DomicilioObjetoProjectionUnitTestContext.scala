package spec.consumers.registrales.domicilio_objeto.unit_test

import akka.actor.ActorSystem
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoMessage.DomicilioObjetoMessageRoots
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents.DomicilioObjetoUpdatedFromDto
import consumers.registral.domicilio_objeto.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class DomicilioObjetoProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[DomicilioObjetoEvents, DomicilioObjetoMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: DomicilioObjetoUpdatedFromDto =>
      (
        DomicilioObjetoMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto, e.domicilioObjetoId).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[DomicilioObjetoEvents, DomicilioObjetoMessageRoots] =
    new DomicilioObjetoProjectionUnitTestKit(cassandraTestkit)
}
