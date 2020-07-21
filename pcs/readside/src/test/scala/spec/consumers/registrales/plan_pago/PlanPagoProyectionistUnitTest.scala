package spec.consumers.registrales.plan_pago

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.plan_pago.application.entities.PlanPagoMessage.PlanPagoMessageRoots
import consumers.registral.plan_pago.domain.PlanPagoEvents
import consumers.registral.plan_pago.domain.PlanPagoEvents.PlanPagoUpdatedFromDto
import consumers.registral.plan_pago.infrastructure.json._
import design_principles.projection
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.registrales.plan_pago.projections.PlanPagoUpdatedFromDtoProjection
import spec.consumers.ProjectionTestkit.ProjectionTestkitMock
import scala.concurrent.Future

import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import readside.proyectionists.registrales.plan_pago.PlanPagoProjectionHandler

class PlanPagoProyectionistUnitTest extends PlanPagoProyectionistSpec {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
    case e: PlanPagoUpdatedFromDto =>
      (
        PlanPagoMessageRoots(
          e.sujetoId,
          e.objetoId,
          e.tipoObjeto,
          e.planPagoId
        ).toString,
        serialization encode e
      )
  })

  override val ProjectionTestkit =
    new PlanPagoProyectionistUnitTest.PlanPagoProjectionTestkit(cassandraTestkit)

}

object PlanPagoProyectionistUnitTest extends ScalaFutures {

  class PlanPagoProjectionTestkit(c: CassandraTestkitMock)(implicit system: ActorSystem)
      extends ProjectionTestkitMock[PlanPagoEvents, PlanPagoMessageRoots] {

    override val cassandraTestkit: CassandraTestkitMock = c

    type Snapshot = PlanPagoUpdatedFromDto
    val decode: String => Snapshot = serialization.decodeF[Snapshot]
    type Projection = PlanPagoUpdatedFromDtoProjection
    val project: Snapshot => Projection = PlanPagoUpdatedFromDtoProjection.apply

    override def process(envelope: EventEnvelope[PlanPagoEvents]): Future[Done] =
      plan_pagoProyectionist process envelope

    def plan_pagoProyectionist: PlanPagoProjectionHandler =
      new readside.proyectionists.registrales.plan_pago.PlanPagoProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }
  }
}
