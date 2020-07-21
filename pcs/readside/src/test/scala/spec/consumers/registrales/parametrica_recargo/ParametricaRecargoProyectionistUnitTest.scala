package spec.consumers.registrales.parametrica_recargo

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage.ParametricaRecargoMessageRoots
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto
import consumers.registral.parametrica_recargo.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.parametrica_recargo.ParametricaRecargoProjectionHandler
import readside.proyectionists.registrales.parametrica_recargo.projections.ParametricaRecargoUpdatedFromDtoProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class ParametricaRecargoProyectionistUnitTest extends ParametricaRecargoProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ParametricaRecargoUpdatedFromDto =>
      (
        ParametricaRecargoMessageRoots(e.bprIndice).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new ParametricaRecargoProyectionistUnitTest.ParametricaRecargoProjectionTestkit(cassandraTestkit)

}

object ParametricaRecargoProyectionistUnitTest extends ScalaFutures {

  class ParametricaRecargoProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[ParametricaRecargoEvents, ParametricaRecargoMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = ParametricaRecargoUpdatedFromDto
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = ParametricaRecargoUpdatedFromDtoProjection
    val project: Snapshot => Projection = ParametricaRecargoUpdatedFromDtoProjection.apply

    override def process(envelope: EventEnvelope[ParametricaRecargoEvents]): Future[Done] =
      parametrica_recargoProyectionist process envelope

    def parametrica_recargoProyectionist: ParametricaRecargoProjectionHandler =
      new readside.proyectionists.registrales.parametrica_recargo.ParametricaRecargoProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
