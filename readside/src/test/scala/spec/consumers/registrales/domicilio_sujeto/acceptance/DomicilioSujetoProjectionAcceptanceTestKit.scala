package spec.consumers.registrales.domicilio_sujeto.acceptance

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage.DomicilioSujetoMessageRoots
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import monitoring.DummyMonitoring
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit

class DomicilioSujetoProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[DomicilioSujetoEvents, DomicilioSujetoMessageRoots]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkitProduction = c
  override def process(envelope: EventEnvelope[DomicilioSujetoEvents]): Future[Done] =
    projectionHandler process envelope

  override def read(e: DomicilioSujetoMessageRoots): Map[String, String] = {

    val sujetoId = e.sujetoId
    val domicilioId = e.domicilioId
    val query = "select * from read_side.buc_domicilios_sujeto" +
      s" where " +
      s"bds_suj_identificador = '$sujetoId' " +
      s"and bds_dom_id = '$domicilioId' " +
      "ALLOW FILTERING"

    cassandraTestkit.cassandraRead.getRow(query).futureValue.get
  }

  def projectionHandler =
    new readside.proyectionists.registrales.domicilio_sujeto.DomicilioSujetoProjectionHandler(new DummyMonitoring)
}
