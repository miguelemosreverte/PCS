package spec.consumers.no_registrales.sujeto

import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.{ProjectionTestContext, ProjectionTestSuite}
import stubs.consumers.no_registrales.sujeto.SujetoEvents._

trait SujetoProjectionSpec extends ProjectionTestSuite[SujetoEvents, SujetoMessageRoots] {

  implicit val tableName: TableName = TableName("read_side.buc_sujeto")

  def validateSujetoReadside(context: ProjectionTestContext[SujetoEvents, SujetoMessageRoots]): Any => Any = {
    case snapshot: SujetoEvents.SujetoSnapshotPersisted =>
      val mappedEvent: Map[String, String] =
        context.ProjectionTestkit read SujetoMessageRoots(snapshot.sujetoId)

      snapshot.registro match {
        case Some(registro) =>
          mappedEvent =========================
            Map(
              "suj_identificador" -> registro.SUJ_IDENTIFICADOR,
              "suj_cat_suj_id" -> registro.SUJ_CAT_SUJ_ID,
              "suj_denominacion" -> registro.SUJ_DENOMINACION,
              "suj_dfe" -> registro.SUJ_DFE,
              "suj_direccion" -> registro.SUJ_DIRECCION,
              "suj_email" -> registro.SUJ_EMAIL,
              "suj_id_externo" -> registro.SUJ_ID_EXTERNO,
              "suj_otros_atributos" -> registro.SUJ_OTROS_ATRIBUTOS,
              "suj_riesgo_fiscal" -> registro.SUJ_RIESGO_FISCAL,
              "suj_situacion_fiscal" -> registro.SUJ_SITUACION_FISCAL,
              "suj_telefono" -> registro.SUJ_TELEFONO,
              "suj_tipo" -> registro.SUJ_TIPO,
              "suj_saldo" -> snapshot.saldo
            )
        case None =>
          mappedEvent =========================
            Map(
              "suj_identificador" -> snapshot.sujetoId,
              "suj_saldo" -> snapshot.saldo
            )
      }
    case _ => ()
  }

  "Updating SujetoTri" should "change read_side.buc_sujeto" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val sujetoId = "Sujeto" + "1" + this.getClass.getName
    val sujetoDto = sujetoUpdatedFromDtoTriStub.copy(
      registro = sujetoUpdatedFromDtoTriStub.registro.copy(
        SUJ_IDENTIFICADOR = sujetoId
      )
    )
    val snapshot = sujetoSnapshotPersisted.copy(
      sujetoId = sujetoId,
      registro = Some(sujetoDto.registro)
    )

    projectionTestkit processEnvelope projectionTestkit.eventEnvelope(snapshot)

    validateSujetoReadside(context)(snapshot)

    context.close()
  }

  "Updating SujetoAnt" should "change read_side.buc_sujeto" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val sujetoId = "Sujeto" + "2" + this.getClass.getName
    val sujetoDto = sujetoUpdatedFromDtoAntStub.copy(
      registro = sujetoUpdatedFromDtoAntStub.registro.copy(
        SUJ_IDENTIFICADOR = sujetoId
      )
    )
    val snapshot = sujetoSnapshotPersisted.copy(
      sujetoId = sujetoId,
      registro = Some(sujetoDto.registro)
    )

    projectionTestkit processEnvelope projectionTestkit.eventEnvelope(snapshot)

    validateSujetoReadside(context)(snapshot)

    context.close()
  }
}
