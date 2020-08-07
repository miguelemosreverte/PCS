package spec.consumers.no_registrales.obligacion.unit_test

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.domain.ObligacionEvents.{ObligacionPersistedSnapshot, ObligacionRemoved}
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import readside.proyectionists.no_registrales.obligacion.ObligacionProjectionHandler
import readside.proyectionists.no_registrales.obligacion.projectionists.ObligacionSnapshotProjection
import spec.testkit.ProjectionTestkitMock
import consumers.no_registral.obligacion.infrastructure.json._
import monitoring.DummyMonitoring
import akka.actor.typed.scaladsl.adapter._

class ObligacionProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[ObligacionEvents, ObligacionMessageRoots] {

  val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = ObligacionPersistedSnapshot
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = ObligacionSnapshotProjection
  val project: Snapshot => Projection = ObligacionSnapshotProjection.apply

  override def process(envelope: EventEnvelope[ObligacionEvents]): Future[Done] =
    envelope.event match {
      case e: ObligacionRemoved =>
        cassandraTestkit.rowsAsMap.remove(
          ObligacionMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto, e.obligacionId).toString
        )
        Future.successful(Done)
      case _ => obligacionProyectionist process envelope
    }

  def obligacionProyectionist: ObligacionProjectionHandler =
    new readside.proyectionists.no_registrales.obligacion.ObligacionProjectionHandler(
      ObligacionProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    ) {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
