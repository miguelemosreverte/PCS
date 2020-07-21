package spec.consumers.registrales.exencion

import consumers.no_registral.objeto.application.entities.{ObjetoExternalDto, ObjetoMessage}
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import infrastructure.cassandra.CassandraTestkit.TableName
import spec.ProyectionistSpec
import spec.consumers.registrales.exencion.ExencionProyectionistSpec.ExencionMessageRoot

trait ExencionProyectionistSpec extends ProyectionistSpec[ObjetoAddedExencion, ExencionMessageRoot] {
  implicit val tableName: TableName = TableName("read_side.buc_exenciones")

  "ExencionProyectionistSpec" should
  "add a registro" in {

    val evento: ObjetoAddedExencion =
      stubs.consumers.no_registrales.objeto.ObjetoEvents.objetoAddedExencionStub

    ProjectionTestkit process eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
    ProjectionTestkit read ExencionMessageRoot(
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

  }
}

object ExencionProyectionistSpec {
  case class ExencionMessageRoot(
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      exencionId: String
  ) extends ObjetoMessage {
    override def aggregateRoot: String = s"${super.aggregateRoot}-Exencion-$exencionId"
  }
}
