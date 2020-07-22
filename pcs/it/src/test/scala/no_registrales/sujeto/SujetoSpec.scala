package no_registrales.sujeto
import akka.actor.ActorSystem
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import infrastructure.cassandra.CassandraTestkit.TableName
import no_registrales.BaseE2ESpec
import infrastructure.cassandra.CassandraTestkit._
import spec.testkit.ProjectionTestkit

trait SujetoSpec extends BaseE2ESpec {

  implicit val tableName: TableName = TableName("read_side.buc_sujeto")

  def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[SujetoEvents, SujetoMessageRoots]

  "sending an obligacion" should "reflect on the api and the database" in parallelActorSystemRunner { implicit s =>
    val context = testContext()


    val obligacion = stubs.consumers.no_registrales.obligacion.ObligacionExternalDtoStub.obligacionesTri.copy(
        BOB_SALDO = 50
    )
    context.messageProducer produceObligacion obligacion
    eventually {
        val response = context.Query getStateObligacion obligacion
        response.saldo should be(obligacion.BOB_SALDO)
    }
    (ProjectionTestkit(context) read obligacion) =========================
      Map(
        "suj_identificador" -> obligacion.BOB_SUJ_IDENTIFICADOR,
        "suj_saldo" -> obligacion.BOB_SALDO
      )
    context.close()
  }
}
