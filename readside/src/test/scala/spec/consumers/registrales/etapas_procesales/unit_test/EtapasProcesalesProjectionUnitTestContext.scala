package spec.consumers.registrales.etapas_procesales.unit_test

import akka.actor.ActorSystem
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage.EtapasProcesalesMessageRoots
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents.EtapasProcesalesUpdatedFromDto
import consumers.registral.etapas_procesales.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class EtapasProcesalesProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[EtapasProcesalesEvents, EtapasProcesalesMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: EtapasProcesalesUpdatedFromDto =>
      (
        EtapasProcesalesMessageRoots(e.juicioId, e.etapaId).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[EtapasProcesalesEvents, EtapasProcesalesMessageRoots] =
    new EtapasProcesalesProjectionUnitTestKit(cassandraTestkit)
}
