package spec.consumers.registrales.plan_pago.acceptance

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.plan_pago.application.entities.PlanPagoMessage.PlanPagoMessageRoots
import consumers.registral.plan_pago.domain.PlanPagoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import monitoring.DummyMonitoring
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit
import akka.actor.typed.scaladsl.adapter._
import readside.proyectionists.registrales.plan_pago.PlanPagoProjectionHandler

class PlanPagoProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[PlanPagoEvents, PlanPagoMessageRoots]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkitProduction = c
  override def processEnvelope(envelope: EventEnvelope[PlanPagoEvents]): Future[Done] =
    projectionHandler process envelope

  override def read(e: PlanPagoMessageRoots): Map[String, String] = {
    val sujetoId = e.sujetoId
    val objetoId = e.objetoId
    val tipoObjeto = e.tipoObjeto
    val planPagoId = e.planPagoId

    val query = "select * from read_side.buc_planes_pago" +
      s" where " +
      s"bpl_suj_identificador = '$sujetoId' " +
      s"and bpl_soj_identificador = '$objetoId' " +
      s"and bpl_soj_tipo_objeto = '$tipoObjeto' " +
      s"and bpl_pln_id = '$planPagoId' " +
      "ALLOW FILTERING"

    cassandraTestkit.cassandraRead.getRow(query).futureValue.get
  }

  def projectionHandler =
    new readside.proyectionists.registrales.plan_pago.PlanPagoProjectionHandler(
      PlanPagoProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    )
}
