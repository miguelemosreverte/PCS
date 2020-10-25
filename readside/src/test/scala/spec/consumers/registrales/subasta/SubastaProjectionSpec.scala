package spec.consumers.registrales.subasta

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.registral.plan_pago.application.entities.PlanPagoMessage.PlanPagoMessageRoots
import consumers.registral.subasta.application.entities.SubastaExternalDto
import consumers.registral.subasta.application.entities.SubastaMessage.SubastaMessageRoots
import consumers.registral.subasta.domain.SubastaEvents
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.plan_pago.projections.PlanPagoUpdatedFromDtoProjection
import readside.proyectionists.registrales.subasta.projections.SubastaUpdatedFromDtoProjection

object SubastaProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class SubastaProjectionSpec(
    testContext: ActorSystem => SubastaProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_subastas")

  "SubastaProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento =
      stubs.consumers.registrales.subasta.SubastaEvents.subastaUpdatedFromDtoStub

    context.write
      .writeState(
        SubastaUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          SubastaMessageRoots(
            sujetoId = evento.sujetoId,
            objetoId = evento.objetoId,
            tipoObjeto = evento.tipoObjeto,
            subastaId = evento.subastaId
          ).toString
        )
        .get

    val registro: SubastaExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bsb_auto" -> registro.BSB_AUTO,
        "bsb_fecha_fin" -> registro.BSB_FECHA_FIN,
        "bsb_fecha_inicio" -> registro.BSB_FECHA_INICIO,
        "bsb_suj_identificador_sub" -> registro.BSB_SUJ_IDENTIFICADOR_SUB,
        "bsb_tipo" -> registro.BSB_TIPO
      )
  }
}
