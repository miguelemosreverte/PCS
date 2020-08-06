package spec.consumers.registrales.domicilio_sujeto.unit_test

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage.DomicilioSujetoMessageRoots
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import consumers.registral.domicilio_sujeto.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import monitoring.DummyMonitoring
import readside.proyectionists.registrales.domicilio_sujeto.DomicilioSujetoProjectionHandler
import readside.proyectionists.registrales.domicilio_sujeto.projections.DomicilioSujetoUpdatedFromDtoProjection
import spec.testkit.ProjectionTestkitMock
import akka.actor.typed.scaladsl.adapter._

class DomicilioSujetoProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[DomicilioSujetoEvents, DomicilioSujetoMessageRoots] {

  override val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = DomicilioSujetoUpdatedFromDto
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = DomicilioSujetoUpdatedFromDtoProjection
  val project: Snapshot => Projection = DomicilioSujetoUpdatedFromDtoProjection.apply

  override def process(envelope: EventEnvelope[DomicilioSujetoEvents]): Future[Done] =
    domicilio_sujetoProyectionist process envelope

  def domicilio_sujetoProyectionist: DomicilioSujetoProjectionHandler =
    new readside.proyectionists.registrales.domicilio_sujeto.DomicilioSujetoProjectionHandler(
      DomicilioSujetoProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    ) {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
