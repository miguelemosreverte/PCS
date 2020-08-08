package spec.consumers.registrales.etapas_procesales.unit_test

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage.EtapasProcesalesMessageRoots
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents.EtapasProcesalesUpdatedFromDto
import consumers.registral.etapas_procesales.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import monitoring.DummyMonitoring
import readside.proyectionists.registrales.etapas_procesales.EtapasProcesalesProjectionHandler
import readside.proyectionists.registrales.etapas_procesales.projections.EtapasProcesalesUpdatedFromDtoProjection
import spec.testkit.ProjectionTestkitMock
import akka.actor.typed.scaladsl.adapter._

class EtapasProcesalesProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[EtapasProcesalesEvents, EtapasProcesalesMessageRoots] {

  override val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = EtapasProcesalesUpdatedFromDto
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = EtapasProcesalesUpdatedFromDtoProjection
  val project: Snapshot => Projection = EtapasProcesalesUpdatedFromDtoProjection.apply

  override def processEnvelope(envelope: EventEnvelope[EtapasProcesalesEvents]): Future[Done] =
    etapas_procesalesProyectionist process envelope

  def etapas_procesalesProyectionist: EtapasProcesalesProjectionHandler =
    new readside.proyectionists.registrales.etapas_procesales.EtapasProcesalesProjectionHandler(
      EtapasProcesalesProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    ) {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
