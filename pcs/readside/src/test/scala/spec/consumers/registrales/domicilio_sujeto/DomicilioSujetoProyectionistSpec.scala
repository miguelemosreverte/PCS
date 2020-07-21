package spec.consumers.registrales.domicilio_sujeto

import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoExternalDto
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage.DomicilioSujetoMessageRoots
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.ProjectionTestSuite

trait DomicilioSujetoProyectionistSpec extends ProjectionTestSuite[DomicilioSujetoEvents, DomicilioSujetoMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_domicilios_sujeto")

  "DomicilioSujetoProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val evento =
      stubs.consumers.registrales.domicilio_sujeto.DomicilioSujetoEvents.domicilioSujetoUpdatedFromDtoAntStub

    projectionTestkit process projectionTestkit.eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
      projectionTestkit read DomicilioSujetoMessageRoots(
        evento.sujetoId,
        evento.domicilioSujetoId
      )

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
    context.close()
  }
}
