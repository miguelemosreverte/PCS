package spec.consumers.registrales.parametrica_recargo

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage.ParametricaRecargoMessageRoots
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.parametrica_recargo.projections.ParametricaRecargoUpdatedFromDtoProjection

object ParametricaRecargoProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class ParametricaRecargoProjectionSpec(
    testContext: ActorSystem => ParametricaRecargoProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_param_recargo")

  "ParametricaRecargoProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento =
      stubs.consumers.registrales.parametrica_recargo.ParametricaRecargoEvents.parametricaPlanUpdatedFromDtoAntStub

    context.write
      .writeState(
        ParametricaRecargoUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          ParametricaRecargoMessageRoots(parametricaRecargoId = evento.bprIndice).toString
        )
        .get

    val registro: ParametricaRecargoExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bpr_descripcion" -> registro.BPR_DESCRIPCION,
        "bpr_fecha_hasta" -> registro.BPR_FECHA_HASTA,
        "bpr_valor" -> registro.BPR_VALOR
      )
  }
}
