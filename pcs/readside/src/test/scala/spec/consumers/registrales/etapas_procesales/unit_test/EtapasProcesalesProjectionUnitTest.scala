package spec.consumers.registrales.etapas_procesales.unit_test

import akka.actor.ActorSystem
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesMessage
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import spec.consumers.registrales.etapas_procesales.EtapasProcesalesProyectionistSpec
import spec.testsuite.ProjectionTestContext

class EtapasProcesalesProjectionUnitTest extends EtapasProcesalesProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[EtapasProcesalesEvents, EtapasProcesalesMessage.EtapasProcesalesMessageRoots] =
    new EtapasProcesalesProjectionUnitTestContext
}
