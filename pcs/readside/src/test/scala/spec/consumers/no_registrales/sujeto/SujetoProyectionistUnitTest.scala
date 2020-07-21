package spec.consumers.no_registrales.sujeto

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoSnapshotPersisted
import consumers.no_registral.sujeto.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.no_registrales.sujeto.SujetoProjectionHandler
import readside.proyectionists.no_registrales.sujeto.projections.SujetoSnapshotPersistedProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class SujetoProyectionistUnitTest extends SujetoProyectionistSpec {

  override def ProjectionTestkit =
    new SujetoProyectionistUnitTest.SujetoProjectionTestkit(
      SujetoProyectionistUnitTest.cassandraTestkit
    )

}

object SujetoProyectionistUnitTest extends ScalaFutures {

  val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: SujetoSnapshotPersisted =>
      (
        SujetoMessageRoots(e.sujetoId).toString,
        serialization encode e
      )
  })

  class SujetoProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[SujetoEvents, SujetoMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = SujetoSnapshotPersisted
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = SujetoSnapshotPersistedProjection
    val project: Snapshot => Projection = SujetoSnapshotPersistedProjection.apply

    override def process(envelope: EventEnvelope[SujetoEvents]): Future[Done] = sujetoProyectionist process envelope

    def sujetoProyectionist: SujetoProjectionHandler =
      new readside.proyectionists.no_registrales.sujeto.SujetoProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
