package spec.consumers.registrales.juicio.unit_test

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.juicio.application.entities.JuicioMessage.JuicioMessageRoots
import consumers.registral.juicio.domain.JuicioEvents
import consumers.registral.juicio.domain.JuicioEvents.JuicioUpdatedFromDto
import consumers.registral.juicio.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import readside.proyectionists.registrales.juicio.JuicioProjectionHandler
import readside.proyectionists.registrales.juicio.projections.JuicioUpdatedFromDtoProjection
import spec.testkit.ProjectionTestkitMock

class JuicioProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[JuicioEvents, JuicioMessageRoots] {

  override val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = JuicioUpdatedFromDto
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = JuicioUpdatedFromDtoProjection
  val project: Snapshot => Projection = JuicioUpdatedFromDtoProjection.apply

  override def process(envelope: EventEnvelope[JuicioEvents]): Future[Done] =
    juicioProyectionist process envelope

  def juicioProyectionist: JuicioProjectionHandler =
    new readside.proyectionists.registrales.juicio.JuicioProjectionHandler() {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
