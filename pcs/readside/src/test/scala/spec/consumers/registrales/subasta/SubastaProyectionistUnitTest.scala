package spec.consumers.registrales.subasta

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.subasta.application.entities.SubastaMessage.SubastaMessageRoots
import consumers.registral.subasta.domain.SubastaEvents
import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import consumers.registral.subasta.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.subasta.SubastaProjectionHandler
import readside.proyectionists.registrales.subasta.projections.SubastaUpdatedFromDtoProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class SubastaProyectionistUnitTest extends SubastaProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: SubastaUpdatedFromDto =>
      (
        SubastaMessageRoots(
          e.sujetoId,
          e.objetoId,
          e.tipoObjeto,
          e.subastaId
        ).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new SubastaProyectionistUnitTest.SubastaProjectionTestkit(cassandraTestkit)

}

object SubastaProyectionistUnitTest extends ScalaFutures {

  class SubastaProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[SubastaEvents, SubastaMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = SubastaUpdatedFromDto
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = SubastaUpdatedFromDtoProjection
    val project: Snapshot => Projection = SubastaUpdatedFromDtoProjection.apply

    override def process(envelope: EventEnvelope[SubastaEvents]): Future[Done] =
      subastaProyectionist process envelope

    def subastaProyectionist: SubastaProjectionHandler =
      new readside.proyectionists.registrales.subasta.SubastaProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
