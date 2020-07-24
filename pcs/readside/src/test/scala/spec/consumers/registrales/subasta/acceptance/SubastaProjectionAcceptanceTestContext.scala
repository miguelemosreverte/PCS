package spec.consumers.registrales.subasta.acceptance

import akka.actor.ActorSystem
import consumers.registral.subasta.application.entities.SubastaMessage.SubastaMessageRoots
import consumers.registral.subasta.domain.SubastaEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class SubastaProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[SubastaEvents, SubastaMessageRoots] {

  import system.dispatcher
  truncateTables(
    Seq(
      "buc_subastas"
    )
  )
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[SubastaEvents, SubastaMessageRoots] =
    new SubastaProjectionAcceptanceTestKit(cassandraTestkit)
}
