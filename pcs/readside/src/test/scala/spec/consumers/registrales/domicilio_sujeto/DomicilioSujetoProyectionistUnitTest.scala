package spec.consumers.registrales.domicilio_sujeto

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage.DomicilioSujetoMessageRoots
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import consumers.registral.domicilio_sujeto.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.domicilio_sujeto.DomicilioSujetoProjectionHandler
import readside.proyectionists.registrales.domicilio_sujeto.projections.DomicilioSujetoUpdatedFromDtoProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class DomicilioSujetoProyectionistUnitTest extends DomicilioSujetoProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: DomicilioSujetoUpdatedFromDto =>
      (
        DomicilioSujetoMessageRoots(e.sujetoId, e.domicilioSujetoId).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new DomicilioSujetoProyectionistUnitTest.DomicilioSujetoProjectionTestkit(cassandraTestkit)

}

object DomicilioSujetoProyectionistUnitTest extends ScalaFutures {

  class DomicilioSujetoProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[DomicilioSujetoEvents, DomicilioSujetoMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = DomicilioSujetoUpdatedFromDto
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = DomicilioSujetoUpdatedFromDtoProjection
    val project: Snapshot => Projection = DomicilioSujetoUpdatedFromDtoProjection.apply

    override def process(envelope: EventEnvelope[DomicilioSujetoEvents]): Future[Done] =
      domicilio_sujetoProyectionist process envelope

    def domicilio_sujetoProyectionist: DomicilioSujetoProjectionHandler =
      new readside.proyectionists.registrales.domicilio_sujeto.DomicilioSujetoProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
