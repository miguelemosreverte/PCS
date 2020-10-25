package spec.consumers.no_registrales.sujeto

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.no_registrales.sujeto.projections.SujetoSnapshotPersistedProjection
import stubs.consumers.no_registrales.sujeto.SujetoEvents._

object SujetoProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class SujetoProjectionSpec(
    testContext: ActorSystem => SujetoProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_sujeto")

  "Updating SujetoTri" should "change read_side.buc_sujeto" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val sujetoId = "SujetoTri" + "1" + utils.Inference.getSimpleName(this.getClass.getName)
    val sujetoDto = sujetoUpdatedFromDtoTriStub.copy(
      registro = sujetoUpdatedFromDtoTriStub.registro.copy(
        SUJ_IDENTIFICADOR = sujetoId
      )
    )
    val snapshot = sujetoSnapshotPersisted.copy(
      sujetoId = sujetoId,
      registro = Some(sujetoDto.registro)
    )

    context.write
      .writeState(
        SujetoSnapshotPersistedProjection(snapshot)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          SujetoMessageRoots(
            snapshot.sujetoId
          ).toString
        )
        .get

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
  }

  "Updating SujetoAnt" should "change read_side.buc_sujeto" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val sujetoId = "SujetoAnt" + "2" + utils.Inference.getSimpleName(this.getClass.getName)
    val sujetoDto = sujetoUpdatedFromDtoAntStub.copy(
      registro = sujetoUpdatedFromDtoAntStub.registro.copy(
        SUJ_IDENTIFICADOR = sujetoId
      )
    )
    val snapshot = sujetoSnapshotPersisted.copy(
      sujetoId = sujetoId,
      registro = Some(sujetoDto.registro)
    )

    context.write
      .writeState(
        SujetoSnapshotPersistedProjection(snapshot)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          SujetoMessageRoots(
            snapshot.sujetoId
          ).toString
        )
        .get

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
  }
}
