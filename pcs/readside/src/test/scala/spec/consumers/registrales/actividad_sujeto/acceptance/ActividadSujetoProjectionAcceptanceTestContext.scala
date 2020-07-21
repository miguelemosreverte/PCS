package spec.consumers.registrales.actividad_sujeto.acceptance

import akka.actor.ActorSystem
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage.ActividadSujetoMessageRoots
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class ActividadSujetoProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[ActividadSujetoEvents, ActividadSujetoMessageRoots] {

  import system.dispatcher

  truncateTables(
    Seq(
      "buc_actividades_sujeto"
    )
  )

  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[ActividadSujetoEvents, ActividadSujetoMessageRoots] =
    new ActividadSujetoProjectionAcceptanceTestKit(cassandraTestkit)
}
