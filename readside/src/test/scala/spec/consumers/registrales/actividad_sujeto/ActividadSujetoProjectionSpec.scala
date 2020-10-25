package spec.consumers.registrales.actividad_sujeto

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoExternalDto
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage.ActividadSujetoMessageRoots
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.no_registrales.sujeto.projections.SujetoSnapshotPersistedProjection
import readside.proyectionists.registrales.actividad_sujeto.projections.ActividadSujetoUpdatedFromDtoProjection

object ActividadSujetoProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class ActividadSujetoProjectionSpec(
    testContext: ActorSystem => ActividadSujetoProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_actividades_sujeto")

  "ActividadSujetoProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento =
      stubs.consumers.registrales.actividad_sujeto.ActividadSujetoEvents.actividadSujetoUpdatedFromDtoStub

    context.write
      .writeState(
        ActividadSujetoUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          ActividadSujetoMessageRoots(
            evento.sujetoId,
            evento.actividadSujetoId
          ).toString
        )
        .get

    val registro: ActividadSujetoExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bat_descripcion" -> registro.BAT_DESCRIPCION,
        "bat_fecha_fin" -> registro.BAT_FECHA_FIN,
        "bat_fecha_inicio" -> registro.BAT_FECHA_INICIO,
        "bat_otros_atributos" -> registro.BAT_OTROS_ATRIBUTOS,
        "bat_referencia" -> registro.BAT_REFERENCIA,
        "bat_tipo" -> registro.BAT_TIPO
      )
  }
}
