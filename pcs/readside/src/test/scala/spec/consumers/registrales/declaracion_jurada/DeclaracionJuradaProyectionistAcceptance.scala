package spec.consumers.registrales.declaracion_jurada

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaMessage.DeclaracionJuradaMessageRoots
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import org.scalatest.Ignore
import org.scalatest.concurrent.ScalaFutures
import spec.consumers.ProjectionTestkit
import spec.consumers.registrales.declaracion_jurada.DeclaracionJuradaProyectionistAcceptance.DeclaracionJuradaProjectionTestkit

@Ignore
class DeclaracionJuradaProyectionistAcceptance extends DeclaracionJuradaProyectionistSpec {
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()
  override val ProjectionTestkit = new DeclaracionJuradaProjectionTestkit(cassandraTestkit)

  override def beforeEach(): Unit =
    truncateTables(
      Seq(
        "buc_declaraciones_juradas"
      )
    )
}

object DeclaracionJuradaProyectionistAcceptance {
  class DeclaracionJuradaProjectionTestkit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
      extends ProjectionTestkit[DeclaracionJuradaEvents, DeclaracionJuradaMessageRoots]
      with ScalaFutures {
    override val cassandraTestkit: CassandraTestkitProduction = c
    override def process(envelope: EventEnvelope[DeclaracionJuradaEvents]): Future[Done] =
      projectionHandler process envelope

    override def read(e: DeclaracionJuradaMessageRoots): Map[String, String] = {

      val sujetoId = e.sujetoId
      val objetoId = e.objetoId
      val tipoObjeto = e.tipoObjeto
      val declaracionJuradaId = e.declaracionJuradaId
      val query = "select * from read_side.buc_declaraciones_juradas" +
      s" where " +
      s"bdj_suj_identificador = '$sujetoId' " +
      s"and bdj_soj_identificador = '$objetoId' " +
      s"and bdj_soj_tipo_objeto = '$tipoObjeto' " +
      s"and bdj_ddj_id = '$declaracionJuradaId' " +
      "ALLOW FILTERING"

      cassandraTestkit.cassandraRead.getRow(query).futureValue.get
    }

    def projectionHandler =
      new readside.proyectionists.registrales.declaracion_jurada.DeclaracionJuradaProjectionHandler()
  }
}
