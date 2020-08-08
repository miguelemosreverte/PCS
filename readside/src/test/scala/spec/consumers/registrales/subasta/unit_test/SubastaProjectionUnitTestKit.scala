package spec.consumers.registrales.subasta.unit_test

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.subasta.application.entities.SubastaMessage.SubastaMessageRoots
import consumers.registral.subasta.domain.SubastaEvents
import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import consumers.registral.subasta.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import monitoring.DummyMonitoring
import readside.proyectionists.registrales.subasta.SubastaProjectionHandler
import readside.proyectionists.registrales.subasta.projections.SubastaUpdatedFromDtoProjection
import spec.testkit.ProjectionTestkitMock
import akka.actor.typed.scaladsl.adapter._

class SubastaProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[SubastaEvents, SubastaMessageRoots] {

  override val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = SubastaUpdatedFromDto
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = SubastaUpdatedFromDtoProjection
  val project: Snapshot => Projection = SubastaUpdatedFromDtoProjection.apply

  override def processEnvelope(envelope: EventEnvelope[SubastaEvents]): Future[Done] =
    subastaProyectionist process envelope

  def subastaProyectionist: SubastaProjectionHandler =
    new readside.proyectionists.registrales.subasta.SubastaProjectionHandler(
      SubastaProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    ) {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
