package spec.consumers.registrales.domicilio_sujeto.acceptance

import akka.actor.ActorSystem
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage.DomicilioSujetoMessageRoots
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class DomicilioSujetoProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[DomicilioSujetoEvents, DomicilioSujetoMessageRoots] {

  import system.dispatcher

  truncateTables(
    Seq(
      "buc_domicilios_sujeto"
    )
  )

  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[DomicilioSujetoEvents, DomicilioSujetoMessageRoots] =
    new DomicilioSujetoProjectionAcceptanceTestKit(cassandraTestkit)
}
