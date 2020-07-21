package spec.consumers.registrales.domicilio_objeto

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoMessage.DomicilioObjetoMessageRoots
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents.DomicilioObjetoUpdatedFromDto
import consumers.registral.domicilio_objeto.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.domicilio_objeto.DomicilioObjetoProjectionHandler
import readside.proyectionists.registrales.domicilio_objeto.projections.DomicilioObjetoUpdatedFromDtoProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class DomicilioObjetoProyectionistUnitTest extends DomicilioObjetoProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: DomicilioObjetoUpdatedFromDto =>
      (
        DomicilioObjetoMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto, e.domicilioObjetoId).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new DomicilioObjetoProyectionistUnitTest.DomicilioObjetoProjectionTestkit(cassandraTestkit)

}

object DomicilioObjetoProyectionistUnitTest extends ScalaFutures {

  class DomicilioObjetoProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[DomicilioObjetoEvents, DomicilioObjetoMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = DomicilioObjetoUpdatedFromDto
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = DomicilioObjetoUpdatedFromDtoProjection
    val project: Snapshot => Projection = DomicilioObjetoUpdatedFromDtoProjection.apply

    override def process(envelope: EventEnvelope[DomicilioObjetoEvents]): Future[Done] =
      domicilio_objetoProyectionist process envelope

    def domicilio_objetoProyectionist: DomicilioObjetoProjectionHandler =
      new readside.proyectionists.registrales.domicilio_objeto.DomicilioObjetoProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
