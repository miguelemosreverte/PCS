package spec.consumers.registrales.etapas_procesales.acceptance

import akka.actor.ActorSystem
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage.EtapasProcesalesMessageRoots
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import design_principles.projection.infrastructure.CassandraTestkitProduction
import spec.testkit.ProjectionTestkit
import spec.testsuite.ProjectionTestContext

class EtapasProcesalesProjectionAcceptanceTestContext(implicit system: ActorSystem)
    extends ProjectionTestContext[EtapasProcesalesEvents, EtapasProcesalesMessageRoots] {

  import system.dispatcher

  truncateTables(
    Seq(
      "buc_etapas_proc"
    )
  )

  private val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction.apply()

  override def ProjectionTestkit: ProjectionTestkit[EtapasProcesalesEvents, EtapasProcesalesMessageRoots] =
    new EtapasProcesalesProjectionAcceptanceTestKit(cassandraTestkit)
}
