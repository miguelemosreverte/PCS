package spec.consumers.registrales.subasta

import consumers.registral.subasta.application.entities.SubastaExternalDto
import consumers.registral.subasta.application.entities.SubastaMessage.SubastaMessageRoots
import consumers.registral.subasta.domain.SubastaEvents
import infrastructure.cassandra.CassandraTestkit.TableName
import spec.ProyectionistSpec

trait SubastaProyectionistSpec extends ProyectionistSpec[SubastaEvents, SubastaMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_subastas")

  "SubastaProyectionistSpec" should
  "add a registro" in {

    val evento =
      stubs.consumers.registrales.subasta.SubastaEvents.subastaUpdatedFromDtoStub

    ProjectionTestkit process eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
    ProjectionTestkit read SubastaMessageRoots(
      sujetoId = evento.sujetoId,
      objetoId = evento.objetoId,
      tipoObjeto = evento.tipoObjeto,
      subastaId = evento.subastaId
    )

    val registro: SubastaExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bsb_auto" -> registro.BSB_AUTO,
        "bsb_fecha_fin" -> registro.BSB_FECHA_FIN,
        "bsb_fecha_inicio" -> registro.BSB_FECHA_INICIO,
        "bsb_suj_identificador_sub" -> registro.BSB_SUJ_IDENTIFICADOR_SUB,
        "bsb_tipo" -> registro.BSB_TIPO
      )

  }
}
