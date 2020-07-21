package spec.consumers.registrales.juicio.unit_test

import akka.actor.ActorSystem
import consumers.registral.juicio.application.entities.JuicioMessage
import consumers.registral.juicio.domain.JuicioEvents
import spec.consumers.registrales.juicio.JuicioProyectionistSpec
import spec.testsuite.ProjectionTestContext

class JuicioProjectionUnitTest extends JuicioProyectionistSpec {
  override def testContext()(implicit system: ActorSystem): ProjectionTestContext[JuicioEvents, JuicioMessage.JuicioMessageRoots] =
    new JuicioProjectionUnitTestContext
}
