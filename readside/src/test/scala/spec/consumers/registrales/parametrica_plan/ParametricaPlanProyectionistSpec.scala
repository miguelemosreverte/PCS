package spec.consumers.registrales.parametrica_plan

import consumers.registral.parametrica_plan.application.entities.ParametricaPlanExternalDto
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanMessage.ParametricaPlanMessageRoots
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.ProjectionTestSuite

trait ParametricaPlanProyectionistSpec extends ProjectionTestSuite[ParametricaPlanEvents, ParametricaPlanMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_obligaciones")

  "ParametricaPlanProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val evento =
      stubs.consumers.registrales.parametrica_plan.ParametricaPlanEvents.parametricaPlanUpdatedFromDtoAntStub

    projectionTestkit process projectionTestkit.eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
      projectionTestkit read ParametricaPlanMessageRoots(
        parametricaPlanId = evento.bppFpmId
      )

    val registro: ParametricaPlanExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bpp_decreto" -> registro.BPP_DECRETO,
        "bpp_fpm_descripcion" -> registro.BPP_FPM_DESCRIPCION,
        "bpp_indice_int_financ" -> registro.BPP_INDICE_INT_FINANC,
        "bpp_indice_int_punit" -> registro.BPP_INDICE_INT_PUNIT,
        "bpp_indice_int_resar" -> registro.BPP_INDICE_INT_RESAR,
        "bpp_monto_max_deuda" -> registro.BPP_MONTO_MAX_DEUDA,
        "bpp_monto_min_anticipo" -> registro.BPP_MONTO_MIN_ANTICIPO,
        "bpp_monto_min_cuota" -> registro.BPP_MONTO_MIN_CUOTA,
        "bpp_monto_min_deuda" -> registro.BPP_MONTO_MIN_DEUDA,
        "bpp_porcentaje_anticipo" -> registro.BPP_PORCENTAJE_ANTICIPO
      )

    context.close()
  }
}
