package spec.consumers.registrales.domicilio_objeto

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoMessage.DomicilioObjetoMessageRoots
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import org.scalatest.Ignore
import org.scalatest.concurrent.ScalaFutures
import spec.consumers.ProjectionTestkit
import spec.consumers.registrales.domicilio_objeto.DomicilioObjetoProyectionistAcceptance.DomicilioObjetoProjectionTestkit
@Ignore
class DomicilioObjetoProyectionistAcceptance extends DomicilioObjetoProyectionistSpec {
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()
  override val ProjectionTestkit = new DomicilioObjetoProjectionTestkit(cassandraTestkit)

  override def beforeEach(): Unit =
    truncateTables(
      Seq(
        "buc_domicilios_objeto"
      )
    )
}

object DomicilioObjetoProyectionistAcceptance {
  class DomicilioObjetoProjectionTestkit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
      extends ProjectionTestkit[DomicilioObjetoEvents, DomicilioObjetoMessageRoots]
      with ScalaFutures {
    override val cassandraTestkit: CassandraTestkitProduction = c
    override def process(envelope: EventEnvelope[DomicilioObjetoEvents]): Future[Done] =
      projectionHandler process envelope

    override def read(e: DomicilioObjetoMessageRoots): Map[String, String] = {

      val sujetoId = e.sujetoId
      val objetoId = e.objetoId
      val tipoObjeto = e.tipoObjeto
      val domicilioId = e.domicilioId
      val query = "select * from read_side.buc_domicilios_objeto" +
      s" where " +
      s"bdo_suj_identificador = '$sujetoId' " +
      s"and bdo_soj_identificador = '$objetoId' " +
      s"and bdo_soj_tipo_objeto = '$tipoObjeto' " +
      s"and bdo_dom_id = '$domicilioId' " +
      "ALLOW FILTERING"

      cassandraTestkit.cassandraRead.getRow(query).futureValue.get
    }

    def projectionHandler =
      new readside.proyectionists.registrales.domicilio_objeto.DomicilioObjetoProjectionHandler()
  }
}
