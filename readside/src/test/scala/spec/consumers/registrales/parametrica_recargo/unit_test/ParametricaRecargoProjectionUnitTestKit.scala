package spec.consumers.registrales.parametrica_recargo.unit_test

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage.ParametricaRecargoMessageRoots
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto
import consumers.registral.parametrica_recargo.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import monitoring.DummyMonitoring
import readside.proyectionists.registrales.parametrica_recargo.ParametricaRecargoProjectionHandler
import readside.proyectionists.registrales.parametrica_recargo.projections.ParametricaRecargoUpdatedFromDtoProjection
import spec.testkit.ProjectionTestkitMock

class ParametricaRecargoProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[ParametricaRecargoEvents, ParametricaRecargoMessageRoots] {

  override val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = ParametricaRecargoUpdatedFromDto
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = ParametricaRecargoUpdatedFromDtoProjection
  val project: Snapshot => Projection = ParametricaRecargoUpdatedFromDtoProjection.apply

  override def process(envelope: EventEnvelope[ParametricaRecargoEvents]): Future[Done] =
    parametrica_recargoProyectionist process envelope

  def parametrica_recargoProyectionist: ParametricaRecargoProjectionHandler =
    new readside.proyectionists.registrales.parametrica_recargo.ParametricaRecargoProjectionHandler(new DummyMonitoring) {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
