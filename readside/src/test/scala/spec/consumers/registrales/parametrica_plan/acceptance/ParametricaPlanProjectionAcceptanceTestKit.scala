package spec.consumers.registrales.parametrica_plan.acceptance

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanMessage.ParametricaPlanMessageRoots
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import monitoring.DummyMonitoring
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit

class ParametricaPlanProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[ParametricaPlanEvents, ParametricaPlanMessageRoots]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkitProduction = c
  override def process(envelope: EventEnvelope[ParametricaPlanEvents]): Future[Done] =
    projectionHandler process envelope

  override def read(e: ParametricaPlanMessageRoots): Map[String, String] = {
    val query = "select * from read_side.buc_param_plan" +
      s" where " +
      s"bpp_fpm_id = '${e.parametricaPlanId}' " +
      "ALLOW FILTERING"

    cassandraTestkit.cassandraRead.getRow(query).futureValue.get
  }

  def projectionHandler =
    new readside.proyectionists.registrales.parametrica_plan.ParametricaPlanProjectionHandler(new DummyMonitoring)
}
