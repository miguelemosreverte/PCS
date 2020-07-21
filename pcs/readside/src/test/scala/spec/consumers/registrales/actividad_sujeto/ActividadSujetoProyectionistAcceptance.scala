package spec.consumers.registrales.actividad_sujeto

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage.ActividadSujetoMessageRoots
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import org.scalatest.Ignore
import org.scalatest.concurrent.ScalaFutures
import spec.consumers.ProjectionTestkit
import spec.consumers.registrales.actividad_sujeto.ActividadSujetoProyectionistAcceptance.ActividadSujetoProjectionTestkit

@Ignore
class ActividadSujetoProyectionistAcceptance extends ActividadSujetoProyectionistSpec {
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()
  override val ProjectionTestkit = new ActividadSujetoProjectionTestkit(cassandraTestkit)

  override def beforeEach(): Unit =
    truncateTables(
      Seq(
        "buc_actividades_sujeto"
      )
    )
}

object ActividadSujetoProyectionistAcceptance {
  class ActividadSujetoProjectionTestkit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
      extends ProjectionTestkit[ActividadSujetoEvents, ActividadSujetoMessageRoots]
      with ScalaFutures {
    override val cassandraTestkit = c
    override def process(envelope: EventEnvelope[ActividadSujetoEvents]): Future[Done] =
      projectionHandler process envelope

    override def read(e: ActividadSujetoMessageRoots): Map[String, String] = {

      val sujetoId = e.sujetoId
      val bat_atd_id = e.actividadSujetoId
      val query = "select * from read_side.buc_actividades_sujeto" +
      s" where bat_suj_identificador = '${sujetoId}' " +
      s"and bat_atd_id = '${bat_atd_id}' " +
      "ALLOW FILTERING"

      cassandraTestkit.cassandraRead.getRow(query).futureValue.get
    }

    def projectionHandler =
      new readside.proyectionists.registrales.actividad_sujeto.ActividadSujetoProjectionHandler()
  }
}
