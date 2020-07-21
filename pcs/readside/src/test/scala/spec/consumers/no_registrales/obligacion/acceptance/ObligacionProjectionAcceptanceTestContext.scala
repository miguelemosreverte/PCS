package spec.consumers.no_registrales.obligacion.acceptance

import akka.actor.ActorSystem
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain.ObligacionEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class ObligacionProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[ObligacionEvents, ObligacionMessageRoots] {

  import system.dispatcher

  truncateTables(
    Seq(
      "buc_obligaciones"
    )
  )

  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[ObligacionEvents, ObligacionMessageRoots] =
    new ObligacionProjectionAcceptanceTestKit(cassandraTestkit)
}
