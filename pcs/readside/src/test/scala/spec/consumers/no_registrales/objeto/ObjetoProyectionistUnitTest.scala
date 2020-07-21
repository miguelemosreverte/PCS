package spec.consumers.no_registrales.objeto

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.json.ObjetoSnapshotPersistedF
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler
import readside.proyectionists.no_registrales.objeto.projections.ObjetoSnapshotPersistedProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class ObjetoProyectionistUnitTest extends ObjetoProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ObjetoSnapshotPersisted =>
      (
        ObjetoMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit = new ObjetoProyectionistUnitTest.ObjetoProjectionTestkit(cassandraTestkit)

}

object ObjetoProyectionistUnitTest extends ScalaFutures {

  class ObjetoProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[ObjetoEvents, ObjetoMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = ObjetoSnapshotPersisted
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = ObjetoSnapshotPersistedProjection
    val project: Snapshot => Projection = ObjetoSnapshotPersistedProjection.apply

    override def process(envelope: EventEnvelope[ObjetoEvents]): Future[Done] = objetoProyectionist process envelope

    def objetoProyectionist: ObjetoProjectionHandler =
      new readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
