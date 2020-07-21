package spec.consumers.registrales.parametrica_recargo.acceptance

import akka.actor.ActorSystem
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoMessage.ParametricaRecargoMessageRoots
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class ParametricaRecargoProjectionAcceptanceTestContext(implicit system: ActorSystem)
  extends ProjectionTestContext[ParametricaRecargoEvents, ParametricaRecargoMessageRoots] {

  import system.dispatcher
  truncateTables(
    Seq(
      "buc_param_recargo"
    )
  )
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[ParametricaRecargoEvents, ParametricaRecargoMessageRoots] =
    new ParametricaRecargoProjectionAcceptanceTestKit(cassandraTestkit)
}

