package spec.consumers.registrales.parametrica_plan.unit_test

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanMessage.ParametricaPlanMessageRoots
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents.ParametricaPlanUpdatedFromDto
import consumers.registral.parametrica_plan.infrastructure.json._
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import readside.proyectionists.registrales.parametrica_plan.ParametricaPlanProjectionHandler
import readside.proyectionists.registrales.parametrica_plan.projections.ParametricaPlanUpdatedFromDtoProjection
import spec.testkit.ProjectionTestkitMock

class ParametricaPlanProjectionUnitTestKit(c: CassandraTestkitMock)(implicit system: ActorSystem)
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
