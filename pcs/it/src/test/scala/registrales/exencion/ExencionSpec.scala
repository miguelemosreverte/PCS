package registrales.exencion

import java.time.LocalDateTime

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import design_principles.actor_model.{ActorSpecCassandra, ActorSpecWriteside}
import design_principles.projection.CassandraTestkit
import no_registrales.BaseE2ESpec
import registrales.exencion.testkit.ExencionImplicitConversions
import registrales.exencion.testkit.query.ExencionQueryTestkit
import spec.consumers.registrales.exencion.ExencionProyectionistSpec.ExencionMessageRoot
import stubs.consumers.no_registrales.objeto.ObjetoExternalDto.objetoExencionStub
import infrastructure.cassandra.CassandraTestkit._

trait ExencionSpec
    extends consumers_spec.no_registrales.sujeto.SujetoSpec
    with BaseE2ESpec
    with ExencionImplicitConversions {

  def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): spec.consumers.ProjectionTestkit[ObjetoAddedExencion, ExencionMessageRoot]

  abstract class ExencionE2ETestContext(implicit val system: ActorSystem)
      extends ActorSpecWriteside
      with ActorSpecCassandra {
    def cassandraTestkit: CassandraTestkit
    def Query: ExencionQueryTestkit
    def close(): Unit
  }

  "Exencion" should
  "add exencion" in parallelActorSystemRunner { implicit s =>
    val context = testContext()

    val obligacion = examples.obligacionWithSaldo50.copy(
      BOB_VENCIMIENTO = Some(LocalDateTime.now.plusHours(6)),
      BOB_SALDO = 0
    )
    context.messageProducer produceObligacion obligacion
    eventually {
      val response = context.Query getStateObligacion examples.obligacionWithSaldo50
      response.exenta should be(false)
    }
    val exencion: Exencion = objetoExencionStub.copy(
      EV_ID = 100,
      BEX_SUJ_IDENTIFICADOR = examples.obligacionWithSaldo50.BOB_SUJ_IDENTIFICADOR,
      BEX_SOJ_IDENTIFICADOR = examples.obligacionWithSaldo50.BOB_SOJ_IDENTIFICADOR,
      BEX_SOJ_TIPO_OBJETO = examples.obligacionWithSaldo50.BOB_SOJ_TIPO_OBJETO,
      BEX_EXE_ID = "1",
      BEX_FECHA_INICIO = Some(LocalDateTime.now.minusDays(1)),
      BEX_FECHA_FIN = Some(LocalDateTime.now.plusDays(1))
    )
    val json = serialization.encode[Exencion](exencion)(consumers.no_registral.objeto.infrastructure.json.ExencionF)
    context.messageProducer.produce(List(json), "DGR-COP-EXENCIONES")(_ => ())

    eventually {
      val response = context.Query getStateObligacion examples.obligacionWithSaldo50
      response.exenta should be(true)
    }

    (ProjectionTestkit(context) read exencion) =========================
      Map(
        "bex_descripcion" -> exencion.BEX_DESCRIPCION,
        "bex_fecha_fin" -> exencion.BEX_FECHA_FIN,
        "bex_fecha_inicio" -> exencion.BEX_FECHA_INICIO,
        "bex_periodo" -> exencion.BEX_PERIODO,
        "bex_porcentaje " -> exencion.BEX_PORCENTAJE,
        "bex_tipo" -> exencion.BEX_TIPO
      )
    context.close()
  }
}
