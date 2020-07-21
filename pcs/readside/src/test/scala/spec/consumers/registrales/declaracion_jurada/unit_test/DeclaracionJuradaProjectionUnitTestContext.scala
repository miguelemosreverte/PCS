package spec.consumers.registrales.declaracion_jurada.unit_test

import akka.actor.ActorSystem
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaMessage.DeclaracionJuradaMessageRoots
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents.DeclaracionJuradaUpdatedFromDto
import consumers.registral.declaracion_jurada.infrastructure.json._
import design_principles.projection.mock.CassandraTestkitMock
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class DeclaracionJuradaProjectionUnitTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[DeclaracionJuradaEvents, DeclaracionJuradaMessageRoots] {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: DeclaracionJuradaUpdatedFromDto =>
      (
        DeclaracionJuradaMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto, e.declaracionJuradaId).toString,
        serialization encode e
      )
  })

  override def ProjectionTestkit: ProjectionTestkit[DeclaracionJuradaEvents, DeclaracionJuradaMessageRoots] =
    new DeclaracionJuradaProjectionUnitTestKit(cassandraTestkit)
}
