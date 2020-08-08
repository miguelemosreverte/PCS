package spec.consumers.registrales.actividad_sujeto

import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoExternalDto
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage.ActividadSujetoMessageRoots
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.ProjectionTestSuite

trait ActividadSujetoProyectionistSpec extends ProjectionTestSuite[ActividadSujetoEvents, ActividadSujetoMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_actividades_sujeto")

  "ActividadSujetoProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val evento =
      stubs.consumers.registrales.actividad_sujeto.ActividadSujetoEvents.actividadSujetoUpdatedFromDtoStub

    projectionTestkit processEnvelope projectionTestkit.eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
      projectionTestkit read ActividadSujetoMessageRoots(evento.sujetoId, evento.actividadSujetoId)

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

    context.close()
  }
}
