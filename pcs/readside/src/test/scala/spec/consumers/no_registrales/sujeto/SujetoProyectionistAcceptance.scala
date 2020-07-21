package spec.consumers.no_registrales.sujeto

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import design_principles.projection.CassandraTestkit
import design_principles.projection.infrastructure.CassandraTestkitProduction
import org.scalatest.Ignore
import org.scalatest.concurrent.ScalaFutures
import spec.consumers.ProjectionTestkit

@Ignore
class SujetoProyectionistAcceptance extends SujetoProyectionistSpec {
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()
  override val ProjectionTestkit = new SujetoProyectionistAcceptance.SujetoProjectionTestkit(cassandraTestkit)

  override def beforeEach(): Unit =
    truncateTables(
      Seq(
        "buc_sujeto"
      )
    )

}

object SujetoProyectionistAcceptance {
  class SujetoProjectionTestkit(c: CassandraTestkitProduction)(implicit system: ActorSystem)
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
      new readside.proyectionists.no_registrales.sujeto.SujetoProjectionHandler()
  }
}
