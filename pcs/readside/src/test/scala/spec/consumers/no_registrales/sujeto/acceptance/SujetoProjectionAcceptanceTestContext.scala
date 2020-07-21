package spec.consumers.no_registrales.sujeto.acceptance

import akka.actor.ActorSystem
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class SujetoProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[SujetoEvents, SujetoMessageRoots] {

  import system.dispatcher

  truncateTables(
    Seq(
      "buc_sujeto"
    )
  )

  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[SujetoEvents, SujetoMessageRoots] =
    new SujetoProjectionAcceptanceTestKit(cassandraTestkit)
}
