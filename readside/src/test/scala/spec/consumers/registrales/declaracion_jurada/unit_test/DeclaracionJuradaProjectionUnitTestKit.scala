package spec.consumers.registrales.declaracion_jurada.unit_test

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaMessage.DeclaracionJuradaMessageRoots
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents.DeclaracionJuradaUpdatedFromDto
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import readside.proyectionists.registrales.declaracion_jurada.DeclaracionJuradaProjectionHandler
import readside.proyectionists.registrales.declaracion_jurada.projections.DeclaracionJuradaUpdatedFromDtoProjection
import spec.testkit.ProjectionTestkitMock
import consumers.registral.declaracion_jurada.infrastructure.json._
import monitoring.DummyMonitoring
import akka.actor.typed.scaladsl.adapter._

class DeclaracionJuradaProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[DeclaracionJuradaEvents, DeclaracionJuradaMessageRoots] {

  override val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = DeclaracionJuradaUpdatedFromDto
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = DeclaracionJuradaUpdatedFromDtoProjection
  val project: Snapshot => Projection = DeclaracionJuradaUpdatedFromDtoProjection.apply

  override def process(envelope: EventEnvelope[DeclaracionJuradaEvents]): Future[Done] =
    declaracion_juradaProyectionist process envelope

  def declaracion_juradaProyectionist: DeclaracionJuradaProjectionHandler =
    new readside.proyectionists.registrales.declaracion_jurada.DeclaracionJuradaProjectionHandler(
      DeclaracionJuradaProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    ) {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
