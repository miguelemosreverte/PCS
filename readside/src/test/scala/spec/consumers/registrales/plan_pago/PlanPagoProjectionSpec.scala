package spec.consumers.registrales.plan_pago

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage.ParametricaRecargoMessageRoots
import consumers.registral.plan_pago.application.entities.PlanPagoExternalDto
import consumers.registral.plan_pago.application.entities.PlanPagoMessage.PlanPagoMessageRoots
import consumers.registral.plan_pago.domain.PlanPagoEvents
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.parametrica_recargo.projections.ParametricaRecargoUpdatedFromDtoProjection
import readside.proyectionists.registrales.plan_pago.projections.PlanPagoUpdatedFromDtoProjection

object PlanPagoProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class PlanPagoProjectionSpec(
    testContext: ActorSystem => PlanPagoProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_planes_pago")

  "PlanPagoProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento =
      stubs.consumers.registrales.plan_pago.PlanPagoEvents.PlanPagoUpdatedFromDtoAntStub

    context.write
      .writeState(
        PlanPagoUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          PlanPagoMessageRoots(
            sujetoId = evento.sujetoId,
            objetoId = evento.objetoId,
            tipoObjeto = evento.tipoObjeto,
            planPagoId = evento.planPagoId
          ).toString
        )
        .get

    val registro: PlanPagoExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bpl_cantidad_cuotas" -> registro.BPL_CANTIDAD_CUOTAS,
        "bpl_estado" -> registro.BPL_ESTADO,
        "bpl_fecha_act_deuda" -> registro.BPL_FECHA_ACT_DEUDA,
        "bpl_fecha_emision" -> registro.BPL_FECHA_EMISION,
        "bpl_importe_a_financiar" -> registro.BPL_IMPORTE_A_FINANCIAR,
        "bpl_importe_anticipo" -> registro.BPL_IMPORTE_ANTICIPO,
        "bpl_importe_financiado" -> registro.BPL_IMPORTE_ANTICIPO,
        "bpl_nro_referencia" -> registro.BPL_NRO_REFERENCIA,
        "bpl_tipo" -> registro.BPL_TIPO,
        "bpl_otros_atributos" -> registro.BPL_OTROS_ATRIBUTOS.toString
      )
  }
}
