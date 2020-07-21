package spec.consumers.registrales.juicio.acceptance

import akka.actor.ActorSystem
import consumers.registral.juicio.application.entities.JuicioMessage
import consumers.registral.juicio.domain.JuicioEvents
import org.scalatest.Ignore
import spec.consumers.registrales.juicio.JuicioProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class JuicioProjectionAcceptanceTest extends JuicioProyectionistSpec {
  override def testContext()(implicit system: ActorSystem): ProjectionTestContext[JuicioEvents, JuicioMessage.JuicioMessageRoots] =
    new JuicioProjectionAcceptanceTestContext
}
