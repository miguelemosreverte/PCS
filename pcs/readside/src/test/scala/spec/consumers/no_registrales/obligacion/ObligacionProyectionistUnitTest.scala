package spec.consumers.no_registrales.obligacion
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.domain.ObligacionEvents.{ObligacionPersistedSnapshot, ObligacionRemoved}
import consumers.no_registral.obligacion.infrastructure.json._
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.no_registrales.obligacion.projectionists.ObligacionSnapshotProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock
import spec.consumers.no_registrales.obligacion.ObligacionProyectionistUnitTest.ObligacionProjectionTestkit
import scala.concurrent.Future

import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import readside.proyectionists.no_registrales.obligacion.ObligacionProjectionHandler

class ObligacionProyectionistUnitTest extends ObligacionProyectionistSpec {

  override val ProjectionTestkit = new ObligacionProjectionTestkit(
    ObligacionProyectionistUnitTest.cassandraTestkit
  )
}

object ObligacionProyectionistUnitTest extends ScalaFutures {

  val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ObligacionPersistedSnapshot =>
      (
        ObligacionMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto, e.obligacionId).toString,
        serialization encode e
      )
  })

  class ObligacionProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[ObligacionEvents, ObligacionMessageRoots] {

    val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = ObligacionPersistedSnapshot
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = ObligacionSnapshotProjection
    val project: Snapshot => Projection = ObligacionSnapshotProjection.apply

    override def process(envelope: EventEnvelope[ObligacionEvents]): Future[Done] =
      envelope.event match {
        case e: ObligacionRemoved => SpecialCase.delete(e)
        case _ => obligacionProyectionist process envelope
      }

    def obligacionProyectionist: ObligacionProjectionHandler =
      new readside.proyectionists.no_registrales.obligacion.ObligacionProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }

    object SpecialCase {
      def delete(e: ObligacionRemoved): Future[Done] = {
        cassandraTestkit.rowsAsMap.remove(
          ObligacionMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto, e.obligacionId).toString
        )
        Future.successful(Done)
      }
    }
  }
}
