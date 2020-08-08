package spec.consumers.registrales.parametrica_recargo

import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage.ParametricaRecargoMessageRoots
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.ProjectionTestSuite

trait ParametricaRecargoProyectionistSpec
    extends ProjectionTestSuite[ParametricaRecargoEvents, ParametricaRecargoMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_param_recargo")

  "ParametricaRecargoProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val evento =
      stubs.consumers.registrales.parametrica_recargo.ParametricaRecargoEvents.parametricaPlanUpdatedFromDtoAntStub

    projectionTestkit processEnvelope projectionTestkit.eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
      projectionTestkit read ParametricaRecargoMessageRoots(
        parametricaRecargoId = evento.bprIndice
      )

    val registro: ParametricaRecargoExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bpr_descripcion" -> registro.BPR_DESCRIPCION,
        "bpr_fecha_hasta" -> registro.BPR_FECHA_HASTA,
        "bpr_valor" -> registro.BPR_VALOR
      )

    context.close()
  }
}
