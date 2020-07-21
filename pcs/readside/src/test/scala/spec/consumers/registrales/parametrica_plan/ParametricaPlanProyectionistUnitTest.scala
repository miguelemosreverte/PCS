package spec.consumers.registrales.parametrica_plan

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanMessage.ParametricaPlanMessageRoots
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents.ParametricaPlanUpdatedFromDto
import consumers.registral.parametrica_plan.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.parametrica_plan.ParametricaPlanProjectionHandler
import readside.proyectionists.registrales.parametrica_plan.projections.ParametricaPlanUpdatedFromDtoProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock

class ParametricaPlanProyectionistUnitTest extends ParametricaPlanProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: ParametricaPlanUpdatedFromDto =>
      (
        ParametricaPlanMessageRoots(e.bppFpmId).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new ParametricaPlanProyectionistUnitTest.ParametricaPlanProjectionTestkit(cassandraTestkit)

}

object ParametricaPlanProyectionistUnitTest extends ScalaFutures {

  class ParametricaPlanProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[ParametricaPlanEvents, ParametricaPlanMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = ParametricaPlanUpdatedFromDto
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = ParametricaPlanUpdatedFromDtoProjection
    val project: Snapshot => Projection = ParametricaPlanUpdatedFromDtoProjection.apply

    override def process(envelope: EventEnvelope[ParametricaPlanEvents]): Future[Done] =
      parametrica_planProyectionist process envelope

    def parametrica_planProyectionist: ParametricaPlanProjectionHandler =
      new readside.proyectionists.registrales.parametrica_plan.ParametricaPlanProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
