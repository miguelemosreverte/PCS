package spec.consumers.registrales.etapas_procesales

import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesExternalDto
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage.EtapasProcesalesMessageRoots
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import infrastructure.cassandra.CassandraTestkit.TableName
import spec.ProyectionistSpec

trait EtapasProcesalesProyectionistSpec
    extends ProyectionistSpec[EtapasProcesalesEvents, EtapasProcesalesMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_etapas_procesales")

  "EtapasProcesalesProyectionistSpec" should
  "add a registro" in {

    val evento =
      stubs.consumers.registrales.etapas_procesales.EtapasProcesalesEvents.etapasProcesalesUpdatedFromDtoAntStub

    ProjectionTestkit process eventEnvelope(evento)

    val mappedEvent: Map[String, String] =
    ProjectionTestkit read EtapasProcesalesMessageRoots(
      evento.juicioId,
      evento.etapaId
    )

    val registro: EtapasProcesalesExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bep_descripcion" -> registro.BEP_DESCRIPCION,
        "bep_tipo" -> registro.BEP_TIPO.toString,
        "bep_referencia" -> registro.BEP_REFERENCIA
      )

  }
}
