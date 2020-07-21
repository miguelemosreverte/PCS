package no_registrales.objeto

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.domain.ObjetoEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import no_registrales.BaseE2ESpec
import spec.testkit.ProjectionTestkit

trait ObjetoSpec extends consumers_spec.no_registrales.objeto.ObjetoSpec with BaseE2ESpec {

  def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[ObjetoEvents, ObjetoMessageRoots]

  implicit val tableName: TableName = TableName("read_side.buc_sujeto_objeto")

  // @TODO
  "sending an obligacion" should "reflect on the api and the database" ignore parallelActorSystemRunner { implicit s =>
    val context = testContext()

    val obligacion = examples.obligacionWithSaldo50
    context.messageProducer produceObligacion obligacion
    val response = context.Query getStateObligacion obligacion
    response.saldo should be(obligacion.BOB_SALDO)
    (ProjectionTestkit(context) read obligacion) =========================
      Map(
        // "soj_suj_identificador" -> snapshot.sujetoId,
        // "soj_tipo_objeto" -> snapshot.tipoObjeto,
        // "soj_identificador" -> snapshot.objetoId,
        // "soj_cat_soj_id" -> registro.SOJ_CAT_SOJ_ID,
        // "soj_descripcion" -> registro.SOJ_DESCRIPCION,
        // "soj_estado" -> registro.SOJ_ESTADO,
        // "soj_fecha_fin" -> registro.SOJ_FECHA_FIN,
        // "soj_fecha_inicio" -> registro.SOJ_FECHA_INICIO,
        // "soj_id_externo" -> registro.SOJ_ID_EXTERNO,
        // "soj_otros_atributos" -> registro.SOJ_OTROS_ATRIBUTOS,
        // "soj_base_imponible" -> registro.SOJ_BASE_IMPONIBLE,
        // "soj_saldo" -> snapshot.saldo
      )
    context.close()
  }
}
