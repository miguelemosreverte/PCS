package spec.consumers.registrales.declaracion_jurada

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage.ActividadSujetoMessageRoots
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaExternalDto
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaMessage.DeclaracionJuradaMessageRoots
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.actividad_sujeto.projections.ActividadSujetoUpdatedFromDtoProjection
import readside.proyectionists.registrales.declaracion_jurada.projections.DeclaracionJuradaUpdatedFromDtoProjection

object DeclaracionJuradaProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class DeclaracionJuradaProjectionSpec(
    testContext: ActorSystem => DeclaracionJuradaProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_declaraciones_juradas")

  "DeclaracionJuradaProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento =
      stubs.consumers.registrales.declaracion_jurada.DeclaracionJuradaEvents.declaracionJuradaUpdatedFromDtoStub

    context.write
      .writeState(
        DeclaracionJuradaUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          DeclaracionJuradaMessageRoots(
            evento.sujetoId,
            evento.objetoId,
            evento.tipoObjeto,
            evento.declaracionJuradaId
          ).toString
        )
        .get

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
