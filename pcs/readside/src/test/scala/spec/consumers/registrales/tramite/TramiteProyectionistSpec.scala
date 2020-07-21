package spec.consumers.registrales.tramite

import consumers.registral.tramite.application.entities.TramiteExternalDto.Tramite
import consumers.registral.tramite.application.entities.TramiteMessage.TramiteMessageRoots
import consumers.registral.tramite.domain.TramiteEvents
import infrastructure.cassandra.CassandraTestkit.TableName
import spec.ProyectionistSpec

trait TramiteProyectionistSpec extends ProyectionistSpec[TramiteEvents, TramiteMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_tramites")

  "TramiteProyectionistSpec" should
  "add a registro" in {

    val evento =
      stubs.consumers.registrales.tramite.TramiteEvents.tramiteUpdatedFromDtoStub

    ProjectionTestkit process eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
    ProjectionTestkit read TramiteMessageRoots(
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

  }
}
