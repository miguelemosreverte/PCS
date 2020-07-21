package spec.consumers.registrales.declaracion_jurada

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaMessage.DeclaracionJuradaMessageRoots
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents.DeclaracionJuradaUpdatedFromDto
import consumers.registral.declaracion_jurada.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.declaracion_jurada.DeclaracionJuradaProjectionHandler
import readside.proyectionists.registrales.declaracion_jurada.projections.DeclaracionJuradaUpdatedFromDtoProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class DeclaracionJuradaProyectionistUnitTest extends DeclaracionJuradaProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: DeclaracionJuradaUpdatedFromDto =>
      (
        DeclaracionJuradaMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto, e.declaracionJuradaId).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new DeclaracionJuradaProyectionistUnitTest.DeclaracionJuradaProjectionTestkit(cassandraTestkit)

}

object DeclaracionJuradaProyectionistUnitTest extends ScalaFutures {

  class DeclaracionJuradaProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[DeclaracionJuradaEvents, DeclaracionJuradaMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = DeclaracionJuradaUpdatedFromDto
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = DeclaracionJuradaUpdatedFromDtoProjection
    val project: Snapshot => Projection = DeclaracionJuradaUpdatedFromDtoProjection.apply

    override def process(envelope: EventEnvelope[DeclaracionJuradaEvents]): Future[Done] =
      declaracion_juradaProyectionist process envelope

    def declaracion_juradaProyectionist: DeclaracionJuradaProjectionHandler =
      new readside.proyectionists.registrales.declaracion_jurada.DeclaracionJuradaProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
