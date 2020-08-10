package spec.consumers.no_registrales.objeto.acceptance

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.domain.ObjetoEvents
import design_principles.projection.CassandraTestkit
import design_principles.projection.infrastructure.CassandraTestkitProduction
import monitoring.DummyMonitoring
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit
import akka.actor.typed.scaladsl.adapter._

class ObjetoProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[ObjetoEvents, ObjetoMessageRoots]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkit = c
  override def processEnvelope(envelope: EventEnvelope[ObjetoEvents]): Future[Done] =
    objetoProyectionist process envelope

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
    readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler(new DummyMonitoring, system.toTyped)
}
