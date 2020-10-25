package spec.consumers.registrales.domicilio_objeto

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoExternalDto
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoMessage.DomicilioObjetoMessageRoots
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.domicilio_objeto.projections.DomicilioObjetoUpdatedFromDtoProjection

object DomicilioObjetoProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class DomicilioObjetoProjectionSpec(
    testContext: ActorSystem => DomicilioObjetoProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_domicilios_objeto")

  "DomicilioObjetoProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento =
      stubs.consumers.registrales.domicilio_objeto.DomicilioObjetoEvents.domicilioObjetoUpdatedFromDtoAntStub

    context.write
      .writeState(
        DomicilioObjetoUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          DomicilioObjetoMessageRoots(
            evento.sujetoId,
            evento.objetoId,
            evento.tipoObjeto,
            evento.domicilioId
          ).toString
        )
        .get

    val registro: DomicilioObjetoExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bdo_barrio" -> registro.BDO_BARRIO,
        "bdo_calle" -> registro.BDO_CALLE,
        "bdo_codigo_postal" -> registro.BDO_CODIGO_POSTAL,
        "bdo_dpto" -> registro.BDO_DPTO,
        "bdo_estado" -> registro.BDO_ESTADO,
        "bdo_kilometro" -> registro.BDO_KILOMETRO,
        "bdo_localidad" -> registro.BDO_LOCALIDAD,
        "bdo_lote" -> registro.BDO_LOTE,
        "bdo_manzana" -> registro.BDO_MANZANA,
        "bdo_piso" -> registro.BDO_PISO,
        "bdo_provincia" -> registro.BDO_PROVINCIA,
        "bdo_puerta" -> registro.BDO_PUERTA,
        "bdo_tipo" -> registro.BDO_TIPO,
        "bdo_torre" -> registro.BDO_TORRE,
        "bdo_observaciones" -> registro.BDO_OBSERVACIONES
      )
  }
}
