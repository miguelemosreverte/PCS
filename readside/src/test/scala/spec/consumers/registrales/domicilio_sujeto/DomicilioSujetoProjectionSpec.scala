package spec.consumers.registrales.domicilio_sujeto

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoMessage.DomicilioObjetoMessageRoots
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoExternalDto
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage.DomicilioSujetoMessageRoots
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.domicilio_sujeto.projections.DomicilioSujetoUpdatedFromDtoProjection

object DomicilioSujetoProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class DomicilioSujetoProjectionSpec(
    testContext: ActorSystem => DomicilioSujetoProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_domicilios_sujeto")

  "DomicilioSujetoProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento =
      stubs.consumers.registrales.domicilio_sujeto.DomicilioSujetoEvents.domicilioSujetoUpdatedFromDtoAntStub

    context.write
      .writeState(
        DomicilioSujetoUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          DomicilioSujetoMessageRoots(
            evento.sujetoId,
            evento.domicilioId
          ).toString
        )
        .get

    val registro: DomicilioSujetoExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bds_barrio" -> registro.BDS_BARRIO,
        "bds_calle" -> registro.BDS_CALLE,
        "bds_codigo_postal" -> registro.BDS_CODIGO_POSTAL,
        "bds_dpto" -> registro.BDS_DPTO,
        "bds_estado" -> registro.BDS_ESTADO,
        "bds_kilometro" -> registro.BDS_KILOMETRO,
        "bds_localidad" -> registro.BDS_LOCALIDAD,
        "bds_lote" -> registro.BDS_LOTE,
        "bds_manzana" -> registro.BDS_MANZANA,
        "bds_piso" -> registro.BDS_PISO,
        "bds_provincia" -> registro.BDS_PROVINCIA,
        "bds_puerta" -> registro.BDS_PUERTA,
        "bds_tipo" -> registro.BDS_TIPO,
        "bds_torre" -> registro.BDS_TORRE
      )
  }
}
