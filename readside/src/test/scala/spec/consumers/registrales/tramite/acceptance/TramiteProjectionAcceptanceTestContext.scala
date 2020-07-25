package spec.consumers.registrales.tramite.acceptance

import akka.actor.ActorSystem
import consumers.registral.tramite.application.entities.TramiteMessage.TramiteMessageRoots
import consumers.registral.tramite.domain.TramiteEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class TramiteProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[TramiteEvents, TramiteMessageRoots] {

  import system.dispatcher
  truncateTables(
    Seq(
      "buc_tramites"
    )
  )
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[TramiteEvents, TramiteMessageRoots] =
    new TramiteProjectionAcceptanceTestKit(cassandraTestkit)
}
