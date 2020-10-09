package spec.consumers.registrales.etapas_procesales

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesExternalDto
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage.EtapasProcesalesMessageRoots
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.registrales.etapas_procesales.projections.EtapasProcesalesUpdatedFromDtoProjection

object EtapasProcesalesProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class EtapasProcesalesProjectionSpec(
    testContext: ActorSystem => EtapasProcesalesProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_etapas_procesales")

  "EtapasProcesalesProyectionistSpec" should
  "add a registro" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
    val evento =
      stubs.consumers.registrales.etapas_procesales.EtapasProcesalesEvents.etapasProcesalesUpdatedFromDtoAntStub

    context.write
      .writeState(
        EtapasProcesalesUpdatedFromDtoProjection(evento)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          EtapasProcesalesMessageRoots(
            evento.juicioId,
            evento.etapaId
          ).toString
        )
        .get

    val registro: EtapasProcesalesExternalDto = evento.registro

    mappedEvent =========================
      Map(
        "bep_descripcion" -> registro.BEP_DESCRIPCION,
        "bep_tipo" -> registro.BEP_TIPO.toString,
        "bep_referencia" -> registro.BEP_REFERENCIA
      )
  }
}
