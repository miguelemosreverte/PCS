package spec.consumers.registrales.exencion

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import design_principles.projection.infrastructure.CassandraTestkitProduction
import org.scalatest.Ignore
import org.scalatest.concurrent.ScalaFutures
import spec.consumers.ProjectionTestkit
import spec.consumers.registrales.exencion.ExencionProyectionistAcceptance.ExencionProjectionTestkit
import spec.consumers.registrales.exencion.ExencionProyectionistSpec.ExencionMessageRoot

@Ignore
class ExencionProyectionistAcceptance extends ExencionProyectionistSpec {
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()
  override val ProjectionTestkit = new ExencionProjectionTestkit(cassandraTestkit)

  override def beforeEach(): Unit =
    truncateTables(
      Seq(
        "buc_exenciones"
      )
    )
}

object ExencionProyectionistAcceptance {
  class ExencionProjectionTestkit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
      extends ProjectionTestkit[ObjetoAddedExencion, ExencionMessageRoot]
      with ScalaFutures {
    override val cassandraTestkit: CassandraTestkitProduction = c
    override def process(envelope: EventEnvelope[ObjetoAddedExencion]): Future[Done] =
      projectionHandler process envelope

    override def read(e: ExencionMessageRoot): Map[String, String] = {
      val sujetoId = e.sujetoId
      val objetoId = e.objetoId
      val tipoObjeto = e.tipoObjeto
      val exencion = e.exencionId
      val query = "select * from read_side.buc_exenciones" +
      s" where " +
      s"bex_suj_identificador = '$sujetoId' " +
      s"and bex_soj_tipo_objeto = '$objetoId' " +
      s"and bex_soj_identificador = '$tipoObjeto' " +
      s"and bex_exe_id = '$exencion' " +
      "ALLOW FILTERING"

      cassandraTestkit.cassandraRead.getRow(query).futureValue.get
    }

    def projectionHandler =
      new readside.proyectionists.registrales.exencion.ExencionProjectionHandler()
  }
}
