package spec.consumers.registrales.exencion

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import consumers.no_registral.objeto.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.exencion.ExencionProjectionHandler
import readside.proyectionists.registrales.exencion.projections.ObjetoAddedExencionProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock
import spec.consumers.registrales.exencion.ExencionProyectionistSpec.ExencionMessageRoot

class ExencionProyectionistUnitTest extends ExencionProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ObjetoEvents.ObjetoAddedExencion =>
      (
        ExencionMessageRoot(
          e.sujetoId,
          e.objetoId,
          e.tipoObjeto,
          e.exencion.BEX_EXE_ID
        ).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new ExencionProyectionistUnitTest.ExencionProjectionTestkit(cassandraTestkit)

}

object ExencionProyectionistUnitTest extends ScalaFutures {

  class ExencionProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[ObjetoAddedExencion, ExencionMessageRoot] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = ObjetoAddedExencion
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = ObjetoAddedExencionProjection
    val project: Snapshot => Projection = ObjetoAddedExencionProjection.apply

    override def process(envelope: EventEnvelope[ObjetoAddedExencion]): Future[Done] =
      exencionProyectionist process envelope

    def exencionProyectionist: ExencionProjectionHandler =
      new readside.proyectionists.registrales.exencion.ExencionProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
