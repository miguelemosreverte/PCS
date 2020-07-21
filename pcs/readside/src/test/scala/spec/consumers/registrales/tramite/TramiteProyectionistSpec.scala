package spec.consumers.registrales.tramite

import consumers.registral.tramite.application.entities.TramiteExternalDto.Tramite
import consumers.registral.tramite.application.entities.TramiteMessage.TramiteMessageRoots
import consumers.registral.tramite.domain.TramiteEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.ProjectionTestSuite

trait TramiteProyectionistSpec extends ProjectionTestSuite[TramiteEvents, TramiteMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_tramites")

  "TramiteProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val evento =
      stubs.consumers.registrales.tramite.TramiteEvents.tramiteUpdatedFromDtoStub

    projectionTestkit process projectionTestkit.eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
      projectionTestkit read TramiteMessageRoots(
        sujetoId = evento.sujetoId,
        tramiteId = evento.tramiteId
      )

    val registro: Tramite = evento.registro

    mappedEvent =========================
      Map(
        "btr_archivos" -> registro.BTR_ARCHIVOS,
        "btr_descripcion" -> registro.BTR_DESCRIPCION,
        "btr_estado" -> registro.BTR_ESTADO,
        "btr_fecha_inicio" -> registro.BTR_FECHA_INICIO,
        "btr_otros_atributos" -> registro.BTR_OTROS_ATRIBUTOS,
        "btr_referencia" -> registro.BTR_REFERENCIA,
        "btr_tipo" -> registro.BTR_TIPO
      )

    context.close()
  }
}
