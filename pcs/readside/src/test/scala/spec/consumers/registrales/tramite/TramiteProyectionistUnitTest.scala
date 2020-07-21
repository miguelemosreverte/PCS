package spec.consumers.registrales.tramite

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.tramite.application.entities.TramiteMessage.TramiteMessageRoots
import consumers.registral.tramite.domain.TramiteEvents
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import consumers.registral.tramite.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.tramite.TramiteProjectionHandler
import readside.proyectionists.registrales.tramite.projections.TramiteUpdatedFromDtoProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class TramiteProyectionistUnitTest extends TramiteProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: TramiteUpdatedFromDto =>
      (
        TramiteMessageRoots(
          e.sujetoId,
          e.tramiteId
        ).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new TramiteProyectionistUnitTest.TramiteProjectionTestkit(cassandraTestkit)

}

object TramiteProyectionistUnitTest extends ScalaFutures {

  class TramiteProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[TramiteEvents, TramiteMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = TramiteUpdatedFromDto
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = TramiteUpdatedFromDtoProjection
    val project: Snapshot => Projection = TramiteUpdatedFromDtoProjection.apply

    override def process(envelope: EventEnvelope[TramiteEvents]): Future[Done] =
      tramiteProyectionist process envelope

    def tramiteProyectionist: TramiteProjectionHandler =
      new readside.proyectionists.registrales.tramite.TramiteProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
