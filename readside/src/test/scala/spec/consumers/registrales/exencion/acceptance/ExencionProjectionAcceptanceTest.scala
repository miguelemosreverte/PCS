package spec.consumers.registrales.exencion.acceptance

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.objeto.domain.ObjetoEvents
import org.scalatest.Ignore
import spec.consumers.registrales.exencion.ExencionProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class ExencionProjectionAcceptanceTest extends ExencionProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[ObjetoEvents.ObjetoAddedExencion, ObjetoMessage.ExencionMessageRoot] =
    new ExencionProjectionAcceptanceTestContext
}
