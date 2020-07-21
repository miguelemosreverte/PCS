package spec.consumers.registrales.declaracion_jurada

import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaExternalDto
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaMessage.DeclaracionJuradaMessageRoots
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents
import infrastructure.cassandra.CassandraTestkit.TableName
import spec.ProyectionistSpec

trait DeclaracionJuradaProyectionistSpec
    extends ProyectionistSpec[DeclaracionJuradaEvents, DeclaracionJuradaMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_declaraciones_juradas")

  "DeclaracionJuradaProyectionistSpec" should
  "add a registro" in {

    val evento =
      stubs.consumers.registrales.declaracion_jurada.DeclaracionJuradaEvents.declaracionJuradaUpdatedFromDtoStub

    ProjectionTestkit process eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
    ProjectionTestkit read DeclaracionJuradaMessageRoots(evento.sujetoId,
                                                         evento.objetoId,
                                                         evento.tipoObjeto,
                                                         evento.declaracionJuradaId)

    val registro: DeclaracionJuradaExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bdj_cuota" -> registro.BDJ_CUOTA,
        "bdj_estado" -> registro.BDJ_ESTADO,
        "bdj_fiscalizada" -> registro.BDJ_FISCALIZADA,
        "bdj_impuesto_determinado" -> registro.BDJ_IMPUESTO_DETERMINADO,
        "bdj_obn_id" -> registro.BDJ_OBN_ID,
        "bdj_otros_atributos" -> registro.BDJ_OTROS_ATRIBUTOS.toString,
        "bdj_percepciones" -> registro.BDJ_PERCEPCIONES,
        "bdj_periodo" -> registro.BDJ_PERIODO,
        "bdj_prorroga" -> registro.BDJ_PRORROGA,
        "bdj_recaudaciones" -> registro.BDJ_RECAUDACIONES,
        "bdj_retenciones" -> registro.BDJ_RETENCIONES,
        "bdj_tipo" -> registro.BDJ_TIPO,
        "bdj_total" -> registro.BDJ_TOTAL,
        "bdj_vencimiento" -> registro.BDJ_VENCIMIENTO
      )

  }
}
