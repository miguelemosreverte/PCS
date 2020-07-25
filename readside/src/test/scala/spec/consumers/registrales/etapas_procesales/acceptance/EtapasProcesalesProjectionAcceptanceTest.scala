package spec.consumers.registrales.etapas_procesales.acceptance

import akka.actor.ActorSystem
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import org.scalatest.Ignore
import spec.consumers.registrales.etapas_procesales.EtapasProcesalesProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class EtapasProcesalesProjectionAcceptanceTest extends EtapasProcesalesProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[EtapasProcesalesEvents, EtapasProcesalesMessage.EtapasProcesalesMessageRoots] =
    new EtapasProcesalesProjectionAcceptanceTestContext
}
