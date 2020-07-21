package spec.consumers.no_registrales.obligacion.acceptance

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain.ObligacionEvents
import design_principles.projection.CassandraTestkit
import design_principles.projection.infrastructure.CassandraTestkitProduction
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit

class ObligacionProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[ObligacionEvents, ObligacionMessageRoots]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkit = c
  override def process(envelope: EventEnvelope[ObligacionEvents]): Future[Done] =
    obligacionProyectionist process envelope

  override def read(e: ObligacionMessageRoots): Map[String, String] = {

    val sujetoId = e.sujetoId
    val objetoId = e.objetoId
    val tipoObjeto = e.tipoObjeto
    val obligacionId = e.obligacionId

    val query = "select * from read_side.buc_obligaciones" +
      s" where bob_suj_identificador = '$sujetoId' " +
      s"and bob_soj_identificador = '$objetoId' " +
      s"and bob_soj_tipo_objeto = '$tipoObjeto' " +
      s"and bob_obn_id = '$obligacionId' " +
      "ALLOW FILTERING"

    cassandraTestkit.cassandraRead.getRow(query).futureValue.get
  }

  def obligacionProyectionist =
    new readside.proyectionists.no_registrales.obligacion.ObligacionProjectionHandler()
}
