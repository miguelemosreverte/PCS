package spec.consumers.no_registrales.objeto.acceptance

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.domain.ObjetoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class ObjetoProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[ObjetoEvents, ObjetoMessageRoots] {

  import system.dispatcher

  truncateTables(
    Seq(
      "buc_sujeto_objeto"
    )
  )

  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[ObjetoEvents, ObjetoMessageRoots] =
    new ObjetoProjectionAcceptanceTestKit(cassandraTestkit)
}
