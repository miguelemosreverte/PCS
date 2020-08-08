package spec.consumers.registrales.exencion.acceptance

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ExencionMessageRoot
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import design_principles.projection.infrastructure.CassandraTestkitProduction
import monitoring.DummyMonitoring
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit
import akka.actor.typed.scaladsl.adapter._
import readside.proyectionists.registrales.exencion.ExencionProjectionHandler

class ExencionProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[ObjetoAddedExencion, ExencionMessageRoot]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkitProduction = c
  override def processEnvelope(envelope: EventEnvelope[ObjetoAddedExencion]): Future[Done] =
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
    new readside.proyectionists.registrales.exencion.ExencionProjectionHandler(
      ExencionProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    )
}
