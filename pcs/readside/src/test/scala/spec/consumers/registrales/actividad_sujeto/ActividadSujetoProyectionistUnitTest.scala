package spec.consumers.registrales.actividad_sujeto

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage.ActividadSujetoMessageRoots
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import consumers.registral.actividad_sujeto.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.actividad_sujeto.ActividadSujetoProjectionHandler
import readside.proyectionists.registrales.actividad_sujeto.projections.ActividadSujetoUpdatedFromDtoProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class ActividadSujetoProyectionistUnitTest extends ActividadSujetoProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ActividadSujetoUpdatedFromDto =>
      (
        ActividadSujetoMessageRoots(e.sujetoId, e.actividadSujetoId).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new ActividadSujetoProyectionistUnitTest.ActividadSujetoProjectionTestkit(cassandraTestkit)

}

object ActividadSujetoProyectionistUnitTest extends ScalaFutures {

  class ActividadSujetoProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[ActividadSujetoEvents, ActividadSujetoMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = ActividadSujetoUpdatedFromDto
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = ActividadSujetoUpdatedFromDtoProjection
    val project: Snapshot => Projection = ActividadSujetoUpdatedFromDtoProjection.apply

    override def process(envelope: EventEnvelope[ActividadSujetoEvents]): Future[Done] =
      actividad_sujetoProyectionist process envelope

    def actividad_sujetoProyectionist: ActividadSujetoProjectionHandler =
      new readside.proyectionists.registrales.actividad_sujeto.ActividadSujetoProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
