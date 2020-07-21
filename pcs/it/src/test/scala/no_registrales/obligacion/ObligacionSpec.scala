package no_registrales.obligacion

import akka.actor.ActorSystem
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain.ObligacionEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import no_registrales.BaseE2ESpec

trait ObligacionSpec extends consumers_spec.no_registrales.obligacion.ObligacionSpec with BaseE2ESpec {

  def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): spec.consumers.ProjectionTestkit[ObligacionEvents, ObligacionMessageRoots]

  implicit val tableName: TableName = TableName("read_side.buc_obligaciones")

  "sending an obligacion" should "reflect on the api and the database" in parallelActorSystemRunner { implicit s =>
    val context = testContext()

    val obligacion = examples.obligacionWithSaldo50
    context.messageProducer produceObligacion obligacion
    val response = context.Query getStateObligacion obligacion
    response.saldo should be(obligacion.BOB_SALDO)
    (ProjectionTestkit(context) read obligacion) =========================
      Map(
        "bob_suj_identificador" -> obligacion.BOB_SUJ_IDENTIFICADOR,
        "bob_soj_tipo_objeto" -> obligacion.BOB_SOJ_TIPO_OBJETO,
        "bob_soj_identificador" -> obligacion.BOB_SOJ_IDENTIFICADOR,
        "bob_obn_id" -> obligacion.BOB_OBN_ID,
        "bob_capital" -> obligacion.BOB_CAPITAL,
        "bob_cuota" -> obligacion.BOB_CUOTA,
        "bob_estado" -> obligacion.BOB_ESTADO,
        "bob_concepto" -> obligacion.BOB_CONCEPTO,
        "bob_fiscalizada" -> obligacion.BOB_FISCALIZADA,
        "bob_impuesto" -> obligacion.BOB_IMPUESTO,
        "bob_interes_punit" -> obligacion.BOB_INTERES_PUNIT,
        "bob_interes_resar" -> obligacion.BOB_INTERES_RESAR,
        "bob_jui_id" -> obligacion.BOB_JUI_ID,
        "bob_otros_atributos" -> obligacion.BOB_OTROS_ATRIBUTOS,
        "bob_periodo" -> obligacion.BOB_PERIODO,
        "bob_pln_id" -> obligacion.BOB_PLN_ID,
        "bob_prorroga" -> obligacion.BOB_PRORROGA,
        "bob_tipo" -> obligacion.BOB_TIPO,
        "bob_total" -> obligacion.BOB_TOTAL,
        "bob_vencimiento" -> obligacion.BOB_VENCIMIENTO,
        "bob_saldo" -> obligacion.BOB_SALDO
      )
    context.close()
  }

}
