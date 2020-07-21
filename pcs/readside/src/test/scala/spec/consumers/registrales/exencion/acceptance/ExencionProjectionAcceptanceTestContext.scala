package spec.consumers.registrales.exencion.acceptance

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ExencionMessageRoot
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class ExencionProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[ObjetoAddedExencion, ExencionMessageRoot] {

  import system.dispatcher

  truncateTables(
    Seq(
      "buc_exenciones"
    )
  )
  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[ObjetoAddedExencion, ExencionMessageRoot] =
    new ExencionProjectionAcceptanceTestKit(cassandraTestkit)
}
