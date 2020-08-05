package spec.consumers.registrales.actividad_sujeto.unit_test

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage.ActividadSujetoMessageRoots
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import readside.proyectionists.registrales.actividad_sujeto.ActividadSujetoProjectionHandler
import readside.proyectionists.registrales.actividad_sujeto.projections.ActividadSujetoUpdatedFromDtoProjection
import spec.testkit.ProjectionTestkitMock
import consumers.registral.actividad_sujeto.infrastructure.json._
import monitoring.DummyMonitoring

class ActividadSujetoProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[ActividadSujetoEvents, ActividadSujetoMessageRoots] {

  override val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = ActividadSujetoUpdatedFromDto
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = ActividadSujetoUpdatedFromDtoProjection
  val project: Snapshot => Projection = ActividadSujetoUpdatedFromDtoProjection.apply

  override def process(envelope: EventEnvelope[ActividadSujetoEvents]): Future[Done] =
    actividad_sujetoProyectionist process envelope

  def actividad_sujetoProyectionist: ActividadSujetoProjectionHandler =
    new readside.proyectionists.registrales.actividad_sujeto.ActividadSujetoProjectionHandler(new DummyMonitoring) {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
