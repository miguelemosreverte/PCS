package spec.consumers.no_registrales.sujeto.unit_test

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoSnapshotPersisted
import consumers.no_registral.sujeto.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import monitoring.DummyMonitoring
import readside.proyectionists.no_registrales.sujeto.SujetoProjectionHandler
import readside.proyectionists.no_registrales.sujeto.projections.SujetoSnapshotPersistedProjection
import spec.testkit.ProjectionTestkitMock
import akka.actor.typed.scaladsl.adapter._

class SujetoProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[SujetoEvents, SujetoMessageRoots] {

  override val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = SujetoSnapshotPersisted
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = SujetoSnapshotPersistedProjection
  val project: Snapshot => Projection = SujetoSnapshotPersistedProjection.apply

  override def process(envelope: EventEnvelope[SujetoEvents]): Future[Done] = sujetoProyectionist process envelope

  def sujetoProyectionist: SujetoProjectionHandler =
    new readside.proyectionists.no_registrales.sujeto.SujetoProjectionHandler(
      SujetoProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    ) {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
