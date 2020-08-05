package spec.consumers.registrales.tramite.acceptance

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.tramite.application.entities.TramiteMessage.TramiteMessageRoots
import consumers.registral.tramite.domain.TramiteEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import monitoring.DummyMonitoring
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit

class TramiteProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[TramiteEvents, TramiteMessageRoots]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkitProduction = c
  override def process(envelope: EventEnvelope[TramiteEvents]): Future[Done] =
    projectionHandler process envelope

  def collectionName: String = "read_side.buc_tramites"

  override def read(e: TramiteMessageRoots): Map[String, String] = {
    val sujetoId = e.sujetoId
    val tramiteId = e.tramiteId

    val query = "select * from read_side.buc_tramites" +
      s" where " +
      s"btr_suj_identificador = '$sujetoId' " +
      s"and btr_trm_id = '$tramiteId' " +
      "ALLOW FILTERING"

    cassandraTestkit.cassandraRead.getRow(query).futureValue.get
  }

  def projectionHandler =
    new readside.proyectionists.registrales.tramite.TramiteProjectionHandler(new DummyMonitoring)
}
