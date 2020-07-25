package spec.consumers.registrales.exencion

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ExencionMessageRoot
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.ProjectionTestSuite

trait ExencionProyectionistSpec extends ProjectionTestSuite[ObjetoAddedExencion, ExencionMessageRoot] {
  implicit val tableName: TableName = TableName("read_side.buc_exenciones")

  "ExencionProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val evento: ObjetoAddedExencion =
      stubs.consumers.no_registrales.objeto.ObjetoEvents.objetoAddedExencionStub

    projectionTestkit process projectionTestkit.eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
      projectionTestkit read ExencionMessageRoot(
        evento.sujetoId,
        evento.objetoId,
        evento.tipoObjeto,
        evento.exencion.BEX_EXE_ID
      )

    implicit val registro: ObjetoExternalDto.Exencion = evento.exencion

    mappedEvent =========================
      Map(
        "bex_descripcion" -> registro.BEX_DESCRIPCION,
        "bex_fecha_fin" -> registro.BEX_FECHA_FIN,
        "bex_fecha_inicio" -> registro.BEX_FECHA_INICIO,
        "bex_periodo" -> registro.BEX_PERIODO,
        "bex_porcentaje " -> registro.BEX_PORCENTAJE,
        "bex_tipo" -> registro.BEX_TIPO
      )

    context.close()
  }
}
