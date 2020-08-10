package spec.consumers.registrales.subasta

import consumers.registral.subasta.application.entities.SubastaExternalDto
import consumers.registral.subasta.application.entities.SubastaMessage.SubastaMessageRoots
import consumers.registral.subasta.domain.SubastaEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.ProjectionTestSuite

trait SubastaProyectionistSpec extends ProjectionTestSuite[SubastaEvents, SubastaMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_subastas")

  "SubastaProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val evento =
      stubs.consumers.registrales.subasta.SubastaEvents.subastaUpdatedFromDtoStub

    projectionTestkit processEnvelope projectionTestkit.eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
      projectionTestkit read SubastaMessageRoots(
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

    context.close()
  }
}
