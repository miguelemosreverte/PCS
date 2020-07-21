package spec.consumers.registrales.declaracion_jurada.acceptance

import akka.actor.ActorSystem
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaMessage.DeclaracionJuradaMessageRoots
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class DeclaracionJuradaProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[DeclaracionJuradaEvents, DeclaracionJuradaMessageRoots] {

  import system.dispatcher

  truncateTables(
    Seq(
      "buc_declaraciones_juradas"
    )
  )

  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[DeclaracionJuradaEvents, DeclaracionJuradaMessageRoots] =
    new DeclaracionJuradaProjectionAcceptanceTestKit(cassandraTestkit)
}
