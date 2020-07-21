package spec.consumers.registrales.juicio

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.juicio.application.entities.JuicioMessage.JuicioMessageRoots
import consumers.registral.juicio.domain.JuicioEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import org.scalatest.Ignore
import org.scalatest.concurrent.ScalaFutures
import spec.consumers.ProjectionTestkit
import spec.consumers.registrales.juicio.JuicioProyectionistAcceptance.JuicioProjectionTestkit

@Ignore
class JuicioProyectionistAcceptance extends JuicioProyectionistSpec {
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()
  override val ProjectionTestkit = new JuicioProjectionTestkit(cassandraTestkit)

  override def beforeEach(): Unit =
    truncateTables(
      Seq(
        "buc_juicios"
      )
    )
}

object JuicioProyectionistAcceptance {
  class JuicioProjectionTestkit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
      extends ProjectionTestkit[JuicioEvents, JuicioMessageRoots]
      with ScalaFutures {
    override val cassandraTestkit: CassandraTestkitProduction = c
    override def process(envelope: EventEnvelope[JuicioEvents]): Future[Done] =
      projectionHandler process envelope

    override def read(e: JuicioMessageRoots): Map[String, String] = {
      val sujetoId = e.sujetoId
      val objetoId = e.objetoId
      val tipoObjeto = e.tipoObjeto
      val juicioId = e.juicioId
      val query = "select * from read_side.buc_juicios" +
      s" where " +
      s"bju_suj_identificador = '$sujetoId' " +
      s"and bju_soj_identificador = '$objetoId' " +
      s"and bju_soj_tipo_objeto = '$tipoObjeto' " +
      s"and bju_jui_id = '$juicioId' " +
      "ALLOW FILTERING"

      cassandraTestkit.cassandraRead.getRow(query).futureValue.get
    }

    def projectionHandler =
      new readside.proyectionists.registrales.juicio.JuicioProjectionHandler()
  }
}
