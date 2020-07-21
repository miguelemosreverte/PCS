package spec.consumers.registrales.etapas_procesales

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage.EtapasProcesalesMessageRoots
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents.EtapasProcesalesUpdatedFromDto
import consumers.registral.etapas_procesales.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.etapas_procesales.EtapasProcesalesProjectionHandler
import readside.proyectionists.registrales.etapas_procesales.projections.EtapasProcesalesUpdatedFromDtoProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class EtapasProcesalesProyectionistUnitTest extends EtapasProcesalesProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: EtapasProcesalesUpdatedFromDto =>
      (
        EtapasProcesalesMessageRoots(e.juicioId, e.etapaId).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new EtapasProcesalesProyectionistUnitTest.EtapasProcesalesProjectionTestkit(cassandraTestkit)

}

object EtapasProcesalesProyectionistUnitTest extends ScalaFutures {

  class EtapasProcesalesProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[EtapasProcesalesEvents, EtapasProcesalesMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = EtapasProcesalesUpdatedFromDto
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = EtapasProcesalesUpdatedFromDtoProjection
    val project: Snapshot => Projection = EtapasProcesalesUpdatedFromDtoProjection.apply

    override def process(envelope: EventEnvelope[EtapasProcesalesEvents]): Future[Done] =
      etapas_procesalesProyectionist process envelope

    def etapas_procesalesProyectionist: EtapasProcesalesProjectionHandler =
      new readside.proyectionists.registrales.etapas_procesales.EtapasProcesalesProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
