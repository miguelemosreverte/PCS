package spec.consumers.registrales.tramite.unit_test

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.tramite.application.entities.TramiteMessage.TramiteMessageRoots
import consumers.registral.tramite.domain.TramiteEvents
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import consumers.registral.tramite.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import monitoring.DummyMonitoring
import readside.proyectionists.registrales.tramite.TramiteProjectionHandler
import readside.proyectionists.registrales.tramite.projections.TramiteUpdatedFromDtoProjection
import spec.testkit.ProjectionTestkitMock
import akka.actor.typed.scaladsl.adapter._

class TramiteProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[TramiteEvents, TramiteMessageRoots] {

  override val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = TramiteUpdatedFromDto
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = TramiteUpdatedFromDtoProjection
  val project: Snapshot => Projection = TramiteUpdatedFromDtoProjection.apply

  override def processEnvelope(envelope: EventEnvelope[TramiteEvents]): Future[Done] =
    tramiteProyectionist process envelope

  def tramiteProyectionist: TramiteProjectionHandler =
    new readside.proyectionists.registrales.tramite.TramiteProjectionHandler(
      TramiteProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    ) {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
