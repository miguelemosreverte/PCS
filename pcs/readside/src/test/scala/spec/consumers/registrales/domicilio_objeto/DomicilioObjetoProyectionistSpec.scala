package spec.consumers.registrales.domicilio_objeto

import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoExternalDto
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoMessage.DomicilioObjetoMessageRoots
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.ProjectionTestSuite

trait DomicilioObjetoProyectionistSpec extends ProjectionTestSuite[DomicilioObjetoEvents, DomicilioObjetoMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_domicilios_objeto")

  "DomicilioObjetoProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val evento =
      stubs.consumers.registrales.domicilio_objeto.DomicilioObjetoEvents.domicilioObjetoUpdatedFromDtoAntStub

    projectionTestkit process projectionTestkit.eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
      projectionTestkit read DomicilioObjetoMessageRoots(
        evento.sujetoId,
        evento.objetoId,
        evento.tipoObjeto,
        evento.domicilioObjetoId
      )

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
    context.close()
  }
}
