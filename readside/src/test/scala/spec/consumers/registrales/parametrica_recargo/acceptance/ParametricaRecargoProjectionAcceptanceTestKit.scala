package spec.consumers.registrales.parametrica_recargo.acceptance

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage.ParametricaRecargoMessageRoots
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import monitoring.DummyMonitoring
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit

class ParametricaRecargoProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[ParametricaRecargoEvents, ParametricaRecargoMessageRoots]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkitProduction = c
  override def process(envelope: EventEnvelope[ParametricaRecargoEvents]): Future[Done] =
    projectionHandler process envelope

  override def read(e: ParametricaRecargoMessageRoots): Map[String, String] = {
    val query = "select * from read_side.buc_param_recargo"

    cassandraTestkit.cassandraRead.getRow(query).futureValue.get
  }

  def projectionHandler =
    new readside.proyectionists.registrales.parametrica_recargo.ParametricaRecargoProjectionHandler(new DummyMonitoring)
}
