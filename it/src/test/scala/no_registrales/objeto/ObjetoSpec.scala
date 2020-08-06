package no_registrales.objeto

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.domain.ObjetoEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import no_registrales.BaseE2ESpec
import spec.testkit.ProjectionTestkit

trait ObjetoSpec extends BaseE2ESpec {

  def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[ObjetoEvents, ObjetoMessageRoots]

  implicit val tableName: TableName = TableName("read_side.buc_sujeto_objeto")

  "sending an obligacion" should "reflect on the api and the database" in parallelActorSystemRunner { implicit s =>
    val context = testContext()

    val obligacion = stubs.consumers.no_registrales.obligacion.ObligacionExternalDtoStub.obligacionesTri.copy(
      BOB_SALDO = 50
    )
    context.messageProducer produceObligacion obligacion
    eventually {
      val response = context.Query getStateObjeto obligacion
      response.saldo should be(obligacion.BOB_SALDO)
    }

    (ProjectionTestkit(context) read obligacion) =========================
      Map(
        "soj_suj_identificador" -> obligacion.BOB_SUJ_IDENTIFICADOR,
        "soj_tipo_objeto" -> obligacion.BOB_SOJ_TIPO_OBJETO,
        "soj_identificador" -> obligacion.BOB_SOJ_IDENTIFICADOR,
        "soj_saldo" -> obligacion.BOB_SALDO
      )

    context.close()
  }
}
