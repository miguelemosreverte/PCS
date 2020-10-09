package spec.consumers.registrales.parametrica_plan

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.registral.juicio.application.entities.JuicioMessage.JuicioMessageRoots
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanExternalDto
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanMessage.ParametricaPlanMessageRoots
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.juicio.projections.JuicioUpdatedFromDtoProjection
import readside.proyectionists.registrales.parametrica_plan.projections.ParametricaPlanUpdatedFromDtoProjection

object ParametricaPlanProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class ParametricaPlanProjectionSpec(
    testContext: ActorSystem => ParametricaPlanProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_obligaciones")

  "ParametricaPlanProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento =
      stubs.consumers.registrales.parametrica_plan.ParametricaPlanEvents.parametricaPlanUpdatedFromDtoAntStub

    context.write
      .writeState(
        ParametricaPlanUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          ParametricaPlanMessageRoots(
            parametricaPlanId = evento.bppFpmId
          ).toString
        )
        .get

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
  }
}
