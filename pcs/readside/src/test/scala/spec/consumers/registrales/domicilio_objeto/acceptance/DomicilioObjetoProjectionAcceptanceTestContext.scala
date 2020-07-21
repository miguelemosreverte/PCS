package spec.consumers.registrales.domicilio_objeto.acceptance

import akka.actor.ActorSystem
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoMessage.DomicilioObjetoMessageRoots
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class DomicilioObjetoProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[DomicilioObjetoEvents, DomicilioObjetoMessageRoots] {

  import system.dispatcher

  truncateTables(
    Seq(
      "buc_domicilios_objeto"
    )
  )

  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[DomicilioObjetoEvents, DomicilioObjetoMessageRoots] =
    new DomicilioObjetoProjectionAcceptanceTestKit(cassandraTestkit)
}
