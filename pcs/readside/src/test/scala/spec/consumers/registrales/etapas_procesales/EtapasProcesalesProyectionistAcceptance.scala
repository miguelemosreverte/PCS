package spec.consumers.registrales.etapas_procesales

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage.EtapasProcesalesMessageRoots
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import org.scalatest.Ignore
import org.scalatest.concurrent.ScalaFutures
import spec.consumers.ProjectionTestkit
import spec.consumers.registrales.etapas_procesales.EtapasProcesalesProyectionistAcceptance.EtapasProcesalesProjectionTestkit

@Ignore
class EtapasProcesalesProyectionistAcceptance extends EtapasProcesalesProyectionistSpec {
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()
  override val ProjectionTestkit = new EtapasProcesalesProjectionTestkit(cassandraTestkit)

  override def beforeEach(): Unit =
    truncateTables(
      Seq(
        "buc_etapas_proc"
      )
    )
}

object EtapasProcesalesProyectionistAcceptance {
  class EtapasProcesalesProjectionTestkit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
      extends ProjectionTestkit[EtapasProcesalesEvents, EtapasProcesalesMessageRoots]
      with ScalaFutures {
    override val cassandraTestkit: CassandraTestkitProduction = c
    override def process(envelope: EventEnvelope[EtapasProcesalesEvents]): Future[Done] =
      projectionHandler process envelope

    override def read(e: EtapasProcesalesMessageRoots): Map[String, String] = {
      val juicioId = e.juicioId
      val etapaId = e.etapaId
      val query = "select * from read_side.buc_etapas_proc" +
      s" where " +
      s"bep_jui_id = '$juicioId' " +
      s"and bpe_eta_id = '$etapaId' " +
      "ALLOW FILTERING"

      cassandraTestkit.cassandraRead.getRow(query).futureValue.get
    }

    def projectionHandler =
      new readside.proyectionists.registrales.etapas_procesales.EtapasProcesalesProjectionHandler()
  }
}
