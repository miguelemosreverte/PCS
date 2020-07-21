package spec.consumers.registrales.parametrica_recargo

import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage.ParametricaRecargoMessageRoots
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents
import infrastructure.cassandra.CassandraTestkit.TableName
import spec.ProyectionistSpec

trait ParametricaRecargoProyectionistSpec
    extends ProyectionistSpec[ParametricaRecargoEvents, ParametricaRecargoMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_param_recargo")

  "ParametricaRecargoProyectionistSpec" should
  "add a registro" in {

    val evento =
      stubs.consumers.registrales.parametrica_recargo.ParametricaRecargoEvents.parametricaPlanUpdatedFromDtoAntStub

    ProjectionTestkit process eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
    ProjectionTestkit read ParametricaRecargoMessageRoots(
      parametricaRecargoId = evento.bprIndice
    )

    val registro: ParametricaRecargoExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bpr_descripcion" -> registro.BPR_DESCRIPCION,
        "bpr_fecha_hasta" -> registro.BPR_FECHA_HASTA,
        "bpr_valor" -> registro.BPR_VALOR
      )

  }
}
