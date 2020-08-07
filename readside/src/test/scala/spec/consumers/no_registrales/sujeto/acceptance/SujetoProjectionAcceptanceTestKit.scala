package spec.consumers.no_registrales.sujeto.acceptance

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import design_principles.projection.CassandraTestkit
import design_principles.projection.infrastructure.CassandraTestkitProduction
import monitoring.DummyMonitoring
import org.scalatest.concurrent.ScalaFutures
import spec.testkit.ProjectionTestkit
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.adapter._
import readside.proyectionists.no_registrales.sujeto.SujetoProjectionHandler

class SujetoProjectionAcceptanceTestKit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
    extends ProjectionTestkit[SujetoEvents, SujetoMessageRoots]
    with ScalaFutures {
  override val cassandraTestkit: CassandraTestkit = c
  override def process(envelope: EventEnvelope[SujetoEvents]): Future[Done] = sujetoProyectionist process envelope

  override def read(e: SujetoMessageRoots): Map[String, String] = {

    val sujetoId = e.sujetoId

    val query = "select * from read_side.buc_sujeto" +
      s" where suj_identificador = '$sujetoId' " +
      "ALLOW FILTERING"

    cassandraTestkit.cassandraRead.getRow(query).futureValue.get
  }

  def sujetoProyectionist =
    new readside.proyectionists.no_registrales.sujeto.SujetoProjectionHandler(
      SujetoProjectionHandler.defaultProjectionSettings(monitoring),
      system.toTyped
    )
}
