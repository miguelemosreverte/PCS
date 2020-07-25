package spec.consumers.registrales.juicio.acceptance

import akka.actor.ActorSystem
import consumers.registral.juicio.application.entities.JuicioMessage.JuicioMessageRoots
import consumers.registral.juicio.domain.JuicioEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class JuicioProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[JuicioEvents, JuicioMessageRoots] {

  import system.dispatcher
  truncateTables(
    Seq(
      "buc_juicios"
    )
  )
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[JuicioEvents, JuicioMessageRoots] =
    new JuicioProjectionAcceptanceTestKit(cassandraTestkit)
}
