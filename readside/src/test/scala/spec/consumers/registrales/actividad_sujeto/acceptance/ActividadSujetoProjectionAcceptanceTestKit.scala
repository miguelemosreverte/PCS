package spec.consumers.registrales.actividad_sujeto.acceptance

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage.ActividadSujetoMessageRoots
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import monitoring.DummyMonitoring
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit
import akka.actor.typed.scaladsl.adapter._
import readside.proyectionists.registrales.actividad_sujeto.ActividadSujetoProjectionHandler

class ActividadSujetoProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[ActividadSujetoEvents, ActividadSujetoMessageRoots]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkitProduction = c
  override def processEnvelope(envelope: EventEnvelope[ActividadSujetoEvents]): Future[Done] =
    projectionHandler process envelope

  override def read(e: ActividadSujetoMessageRoots): Map[String, String] = {

    val sujetoId = e.sujetoId
    val bat_atd_id = e.actividadSujetoId
    val query = "select * from read_side.buc_actividades_sujeto" +
      s" where bat_suj_identificador = '$sujetoId' " +
      s"and bat_atd_id = '$bat_atd_id' " +
      "ALLOW FILTERING"

    cassandraTestkit.cassandraRead.getRow(query).futureValue.get
  }

  def projectionHandler =
    new readside.proyectionists.registrales.actividad_sujeto.ActividadSujetoProjectionHandler(
      ActividadSujetoProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    )
}
