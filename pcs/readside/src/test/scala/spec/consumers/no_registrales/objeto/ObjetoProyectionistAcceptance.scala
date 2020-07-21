package spec.consumers.no_registrales.objeto

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.domain.ObjetoEvents
import design_principles.projection.CassandraTestkit
import org.scalatest.Ignore
import org.scalatest.concurrent.ScalaFutures
import spec.consumers.ProjectionTestkit
import scala.concurrent.Future

import design_principles.projection.infrastructure.CassandraTestkitProduction

@Ignore
class ObjetoProyectionistAcceptance extends ObjetoProyectionistSpec {
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()
  override val ProjectionTestkit = new ObjetoProyectionistAcceptance.ObjetoProjectionTestkit(cassandraTestkit)

  override def beforeEach(): Unit =
    truncateTables(
      Seq(
        "buc_sujeto_objeto"
      )
    )
}

object ObjetoProyectionistAcceptance {
  class ObjetoProjectionTestkit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
      extends ProjectionTestkit[ObjetoEvents, ObjetoMessageRoots]
      with ScalaFutures {
    override val cassandraTestkit: CassandraTestkit = c
    override def process(envelope: EventEnvelope[ObjetoEvents]): Future[Done] = objetoProyectionist process envelope

    override def read(e: ObjetoMessageRoots): Map[String, String] = {

      val sujetoId = e.sujetoId
      val objetoId = e.objetoId
      val tipoObjeto = e.tipoObjeto

      val query = "select * from read_side.buc_sujeto_objeto" +
        s" where soj_suj_identificador = '$sujetoId' " +
        s"and soj_identificador = '$objetoId' " +
        s"and soj_tipo_objeto = '$tipoObjeto' " +
        "ALLOW FILTERING"

      cassandraTestkit.cassandraRead.getRow(query).futureValue.get
    }

    def objetoProyectionist =
      new readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler()
  }
}
