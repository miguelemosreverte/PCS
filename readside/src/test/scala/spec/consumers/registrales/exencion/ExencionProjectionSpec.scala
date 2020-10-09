package spec.consumers.registrales.exencion

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoMessage.{ExencionMessageRoot, ObjetoMessageRoots}
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage.EtapasProcesalesMessageRoots
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.etapas_procesales.projections.EtapasProcesalesUpdatedFromDtoProjection
import readside.proyectionists.registrales.exencion.projections.ObjetoAddedExencionProjection

object ExencionProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class ExencionProjectionSpec(
    testContext: ActorSystem => ExencionProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_exenciones")

  "ExencionProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento: ObjetoAddedExencion =
      stubs.consumers.no_registrales.objeto.ObjetoEvents.objetoAddedExencionStub

    context.write
      .writeState(
        ObjetoAddedExencionProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          ObjetoMessageRoots(
            evento.sujetoId,
            evento.objetoId,
            evento.tipoObjeto
          ).toString
        )
        .get

    implicit val registro: ObjetoExternalDto.Exencion = evento.exencion

    mappedEvent =========================
      Map(
        "bex_descripcion" -> registro.BEX_DESCRIPCION,
        "bex_fecha_fin" -> registro.BEX_FECHA_FIN,
        "bex_fecha_inicio" -> registro.BEX_FECHA_INICIO,
        "bex_periodo" -> registro.BEX_PERIODO,
        "bex_porcentaje" -> registro.BEX_PORCENTAJE,
        "bex_tipo" -> registro.BEX_TIPO
      )
  }
}
