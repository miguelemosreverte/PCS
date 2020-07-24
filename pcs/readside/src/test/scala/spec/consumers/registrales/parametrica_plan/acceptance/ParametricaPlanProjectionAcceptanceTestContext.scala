package spec.consumers.registrales.parametrica_plan.acceptance

import akka.actor.ActorSystem
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanMessage.ParametricaPlanMessageRoots
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class ParametricaPlanProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[ParametricaPlanEvents, ParametricaPlanMessageRoots] {

  import system.dispatcher
  truncateTables(
    Seq(
      "buc_param_plan"
    )
  )
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[ParametricaPlanEvents, ParametricaPlanMessageRoots] =
    new ParametricaPlanProjectionAcceptanceTestKit(cassandraTestkit)
}
