package spec.consumers.registrales.plan_pago

import consumers.registral.plan_pago.application.entities.PlanPagoExternalDto
import consumers.registral.plan_pago.application.entities.PlanPagoMessage.PlanPagoMessageRoots
import consumers.registral.plan_pago.domain.PlanPagoEvents
import infrastructure.cassandra.CassandraTestkit.TableName
import spec.ProyectionistSpec

trait PlanPagoProyectionistSpec extends ProyectionistSpec[PlanPagoEvents, PlanPagoMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_planes_pago")

  "PlanPagoProyectionistSpec" should
  "add a registro" in {

    val evento =
      stubs.consumers.registrales.plan_pago.PlanPagoEvents.PlanPagoUpdatedFromDtoAntStub

    ProjectionTestkit process eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
    ProjectionTestkit read PlanPagoMessageRoots(
      sujetoId = evento.sujetoId,
      objetoId = evento.objetoId,
      tipoObjeto = evento.tipoObjeto,
      planPagoId = evento.planPagoId
    )

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
