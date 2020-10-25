package spec.consumers.no_registrales.objeto

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.domain.ObjetoEvents
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.no_registrales.objeto.projections.ObjetoSnapshotPersistedProjection
import stubs.consumers.no_registrales.objeto.ObjetoEvents._

object ObjetoProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class ObjetoProjectionSpec(
    testContext: ActorSystem => ObjetoProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_sujeto_objeto")

  "Updating ObjetoAnt" should
  "change read_side.buc_sujeto_objeto" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)

    val sujetoId = "Sujeto1" + utils.Inference.getSimpleName(this.getClass.getName)
    val objetoId = "Objeto1Ant" + utils.Inference.getSimpleName(this.getClass.getName)
    val objetoDto = objetoUpdatedFromDtoAntStub.copy(
      sujetoId = sujetoId,
      objetoId = objetoId,
      registro = objetoUpdatedFromDtoAntStub.registro.copy(
        SOJ_SUJ_IDENTIFICADOR = sujetoId,
        SOJ_IDENTIFICADOR = objetoId
      )
    )
    val snapshot = objetoSnapshotPersistedStub.copy(
      sujetoId = objetoDto.sujetoId,
      tipoObjeto = objetoDto.tipoObjeto,
      objetoId = objetoDto.objetoId,
      registro = Some(objetoDto.registro)
    )

    context.write.writeState(ObjetoSnapshotPersistedProjection(snapshot))(s.dispatcher)
    val mappedEvent: Map[String, String] =
      context.read
        .getRow(ObjetoMessageRoots(snapshot.sujetoId, snapshot.objetoId, snapshot.tipoObjeto).toString)
        .get

    snapshot.registro match {
      case Some(registro) =>
        mappedEvent =========================
          Map(
            "soj_suj_identificador" -> snapshot.sujetoId,
            "soj_tipo_objeto" -> snapshot.tipoObjeto,
            "soj_identificador" -> snapshot.objetoId,
            "soj_cat_soj_id" -> registro.SOJ_CAT_SOJ_ID,
            "soj_descripcion" -> registro.SOJ_DESCRIPCION,
            "soj_estado" -> registro.SOJ_ESTADO,
            "soj_fecha_fin" -> registro.SOJ_FECHA_FIN,
            "soj_fecha_inicio" -> registro.SOJ_FECHA_INICIO,
            "soj_id_externo" -> registro.SOJ_ID_EXTERNO,
            "soj_otros_atributos" -> registro.SOJ_OTROS_ATRIBUTOS,
            "soj_base_imponible" -> registro.SOJ_BASE_IMPONIBLE,
            "soj_saldo" -> snapshot.saldo
          )
      case None =>
        mappedEvent =========================
          Map(
            "soj_suj_identificador" -> snapshot.sujetoId,
            "soj_tipo_objeto" -> snapshot.tipoObjeto,
            "soj_identificador" -> snapshot.objetoId,
            "soj_saldo" -> snapshot.saldo
          )

    }

  }

  "Updating ObjetoTri" should
  "change read_side.buc_sujeto_objeto" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)

    val sujetoId = "Sujeto1" + utils.Inference.getSimpleName(this.getClass.getName)
    val objetoId = "Objeto1Tri" + utils.Inference.getSimpleName(this.getClass.getName)
    val objetoDto = objetoUpdatedFromDtoTriStub.copy(
      sujetoId = sujetoId,
      objetoId = objetoId,
      registro = objetoUpdatedFromDtoTriStub.registro.copy(
        SOJ_SUJ_IDENTIFICADOR = sujetoId,
        SOJ_IDENTIFICADOR = objetoId
      )
    )
    val snapshot = objetoSnapshotPersistedStub.copy(
      sujetoId = objetoDto.sujetoId,
      tipoObjeto = objetoDto.tipoObjeto,
      objetoId = objetoDto.objetoId,
      registro = Some(objetoDto.registro)
    )

    context.write.writeState(ObjetoSnapshotPersistedProjection(snapshot))(s.dispatcher)
    val mappedEvent: Map[String, String] =
      context.read
        .getRow(ObjetoMessageRoots(snapshot.sujetoId, snapshot.objetoId, snapshot.tipoObjeto).toString)
        .get

    snapshot.registro match {
      case Some(registro) =>
        mappedEvent =========================
          Map(
            "soj_suj_identificador" -> snapshot.sujetoId,
            "soj_tipo_objeto" -> snapshot.tipoObjeto,
            "soj_identificador" -> snapshot.objetoId,
            "soj_cat_soj_id" -> registro.SOJ_CAT_SOJ_ID,
            "soj_descripcion" -> registro.SOJ_DESCRIPCION,
            "soj_estado" -> registro.SOJ_ESTADO,
            "soj_fecha_fin" -> registro.SOJ_FECHA_FIN,
            "soj_fecha_inicio" -> registro.SOJ_FECHA_INICIO,
            "soj_id_externo" -> registro.SOJ_ID_EXTERNO,
            "soj_otros_atributos" -> registro.SOJ_OTROS_ATRIBUTOS,
            "soj_base_imponible" -> registro.SOJ_BASE_IMPONIBLE,
            "soj_saldo" -> snapshot.saldo
          )
      case None =>
        mappedEvent =========================
          Map(
            "soj_suj_identificador" -> snapshot.sujetoId,
            "soj_tipo_objeto" -> snapshot.tipoObjeto,
            "soj_identificador" -> snapshot.objetoId,
            "soj_saldo" -> snapshot.saldo
          )

    }
  }

}
