package spec.consumers.registrales.tramite

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.registral.subasta.application.entities.SubastaMessage.SubastaMessageRoots
import consumers.registral.tramite.application.entities.TramiteExternalDto.Tramite
import consumers.registral.tramite.application.entities.TramiteMessage.TramiteMessageRoots
import consumers.registral.tramite.domain.TramiteEvents
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.subasta.projections.SubastaUpdatedFromDtoProjection
import readside.proyectionists.registrales.tramite.projections.TramiteUpdatedFromDtoProjection

object TramiteProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class TramiteProjectionSpec(
    testContext: ActorSystem => TramiteProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_tramites")

  "TramiteProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)

    val evento =
      stubs.consumers.registrales.tramite.TramiteEvents.tramiteUpdatedFromDtoStub

    context.write
      .writeState(
        TramiteUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          TramiteMessageRoots(
            sujetoId = evento.sujetoId,
            tramiteId = evento.tramiteId
          ).toString
        )
        .get

    val registro: Tramite = evento.registro

    mappedEvent =========================
      Map(
        "btr_archivos" -> registro.BTR_ARCHIVOS,
        "btr_descripcion" -> registro.BTR_DESCRIPCION,
        "btr_estado" -> registro.BTR_ESTADO,
        "btr_fecha_inicio" -> registro.BTR_FECHA_INICIO,
        "btr_otros_atributos" -> registro.BTR_OTROS_ATRIBUTOS,
        "btr_referencia" -> registro.BTR_REFERENCIA,
        "btr_tipo" -> registro.BTR_TIPO
      )

  }
}
