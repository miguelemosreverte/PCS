package spec.consumers.registrales.subasta.acceptance

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.subasta.application.entities.SubastaMessage.SubastaMessageRoots
import consumers.registral.subasta.domain.SubastaEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit

class SubastaProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[SubastaEvents, SubastaMessageRoots]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkitProduction = c
  override def process(envelope: EventEnvelope[SubastaEvents]): Future[Done] =
    projectionHandler process envelope

  override def read(e: SubastaMessageRoots): Map[String, String] = {
    val sujetoId = e.sujetoId
    val objetoId = e.objetoId
    val tipoObjeto = e.tipoObjeto
    val subastaId = e.subastaId

    val query = "select * from read_side.buc_subastas" +
      s" where " +
      s"bsb_suj_identificador_adq = '$sujetoId' " +
      s"and bsb_soj_identificador = '$tipoObjeto' " +
      s"and bsb_soj_tipo_objeto = '$objetoId' " +
      s"and bsb_sub_id = '$subastaId' " +
      "ALLOW FILTERING"

    cassandraTestkit.cassandraRead.getRow(query).futureValue.get
  }

  def projectionHandler =
    new readside.proyectionists.registrales.subasta.SubastaProjectionHandler()
}
