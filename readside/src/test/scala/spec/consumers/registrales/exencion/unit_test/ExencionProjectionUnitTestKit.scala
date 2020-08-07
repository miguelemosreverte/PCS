package spec.consumers.registrales.exencion.unit_test

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ExencionMessageRoot
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import consumers.no_registral.objeto.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import monitoring.DummyMonitoring
import readside.proyectionists.registrales.exencion.ExencionProjectionHandler
import readside.proyectionists.registrales.exencion.projections.ObjetoAddedExencionProjection
import spec.testkit.ProjectionTestkitMock
import akka.actor.typed.scaladsl.adapter._

class ExencionProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
    extends ProjectionTestkitMock[ObjetoAddedExencion, ExencionMessageRoot] {

  override val cassandraTestkit: CassandraTestkitMock = c

  type Snapshot = ObjetoAddedExencion
  val decode: String => Snapshot = serialization.decodeF[Snapshot]
  type Projection = ObjetoAddedExencionProjection
  val project: Snapshot => Projection = ObjetoAddedExencionProjection.apply

  override def process(envelope: EventEnvelope[ObjetoAddedExencion]): Future[Done] =
    exencionProyectionist process envelope

  def exencionProyectionist: ExencionProjectionHandler =
    new readside.proyectionists.registrales.exencion.ExencionProjectionHandler(
      ExencionProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    ) {
      override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
    }
}
