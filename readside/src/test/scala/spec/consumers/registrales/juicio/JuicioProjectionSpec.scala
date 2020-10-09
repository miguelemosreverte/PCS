package spec.consumers.registrales.juicio

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage.EtapasProcesalesMessageRoots
import consumers.registral.juicio.application.entities.JuicioExternalDto
import consumers.registral.juicio.application.entities.JuicioMessage.JuicioMessageRoots
import consumers.registral.juicio.domain.JuicioEvents
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.etapas_procesales.projections.EtapasProcesalesUpdatedFromDtoProjection
import readside.proyectionists.registrales.juicio.projections.JuicioUpdatedFromDtoProjection

object JuicioProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class JuicioProjectionSpec(
    testContext: ActorSystem => JuicioProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_juicios")

  "JuicioProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento =
      stubs.consumers.registrales.juicio.JuicioEvents.juicioUpdatedFromDtoTriStub

    context.write
      .writeState(
        JuicioUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          JuicioMessageRoots(
            evento.sujetoId,
            evento.objetoId,
            evento.tipoObjeto,
            evento.juicioId
          ).toString
        )
        .get

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
  }
}
