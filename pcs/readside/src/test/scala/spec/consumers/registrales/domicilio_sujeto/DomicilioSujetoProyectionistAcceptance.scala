package spec.consumers.registrales.domicilio_sujeto

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage.DomicilioSujetoMessageRoots
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import org.scalatest.Ignore
import org.scalatest.concurrent.ScalaFutures
import spec.consumers.ProjectionTestkit
import spec.consumers.registrales.domicilio_sujeto.DomicilioSujetoProyectionistAcceptance.DomicilioSujetoProjectionTestkit

@Ignore
class DomicilioSujetoProyectionistAcceptance extends DomicilioSujetoProyectionistSpec {
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()
  override val ProjectionTestkit = new DomicilioSujetoProjectionTestkit(cassandraTestkit)

  override def beforeEach(): Unit =
    truncateTables(
      Seq(
        "buc_domicilios_sujeto"
      )
    )
}

object DomicilioSujetoProyectionistAcceptance {
  class DomicilioSujetoProjectionTestkit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
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
      new readside.proyectionists.registrales.domicilio_sujeto.DomicilioSujetoProjectionHandler()
  }
}
