package spec.consumers.registrales.juicio

import consumers.registral.juicio.application.entities.JuicioExternalDto
import consumers.registral.juicio.application.entities.JuicioMessage.JuicioMessageRoots
import consumers.registral.juicio.domain.JuicioEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.ProjectionTestSuite

trait JuicioProyectionistSpec extends ProjectionTestSuite[JuicioEvents, JuicioMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_juicios")

  "JuicioProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val evento =
      stubs.consumers.registrales.juicio.JuicioEvents.juicioUpdatedFromDtoTriStub

    projectionTestkit processEnvelope projectionTestkit.eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
      projectionTestkit read JuicioMessageRoots(
        evento.sujetoId,
        evento.objetoId,
        evento.tipoObjeto,
        evento.juicioId
      )

    val registro: JuicioExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bju_capital" -> registro.BJU_CAPITAL,
        "bju_estado" -> registro.BJU_ESTADO,
        "bju_fiscalizada" -> registro.BJU_FISCALIZADA,
        "bju_gastos" -> registro.BJU_GASTOS,
        "bju_gastos_mart" -> registro.BJU_GASTOS_MART,
        "bju_honorarios" -> registro.BJU_HONORARIOS,
        "bju_honorarios_mart" -> registro.BJU_HONORARIOS_MART,
        "bju_inicio_demanda" -> registro.BJU_INICIO_DEMANDA,
        "bju_interes_punit" -> registro.BJU_INTERES_PUNIT,
        "bju_interes_resar" -> registro.BJU_INTERES_RESAR,
        "bju_otros_atributos" -> registro.BJU_OTROS_ATRIBUTOS.toString,
        "bju_pcr_id" -> registro.BJU_PCR_ID,
        "bju_porcentaje_iva" -> registro.BJU_PORCENTAJE_IVA,
        "bju_procurador" -> registro.BJU_PROCURADOR,
        "bju_tipo" -> registro.BJU_TIPO,
        "bju_total" -> registro.BJU_TOTAL
      )

    context.close()
  }
}
