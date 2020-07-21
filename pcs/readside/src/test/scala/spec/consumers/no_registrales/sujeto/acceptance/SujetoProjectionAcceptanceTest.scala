package spec.consumers.no_registrales.sujeto.acceptance

import akka.actor.ActorSystem
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import org.scalatest.Ignore
import spec.consumers.no_registrales.sujeto.SujetoProjectionSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class SujetoProjectionAcceptanceTest extends SujetoProjectionSpec {
  override def testContext()(implicit system: ActorSystem): ProjectionTestContext[SujetoEvents, SujetoMessageRoots] =
    new SujetoProjectionAcceptanceTestContext
}
