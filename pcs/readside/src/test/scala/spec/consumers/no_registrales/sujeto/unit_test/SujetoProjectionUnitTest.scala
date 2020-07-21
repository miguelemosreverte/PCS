package spec.consumers.no_registrales.sujeto.unit_test

import akka.actor.ActorSystem
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import spec.consumers.no_registrales.sujeto.SujetoProjectionSpec
import spec.testsuite.ProjectionTestContext

class SujetoProjectionUnitTest extends SujetoProjectionSpec {

  override def testContext()(implicit system: ActorSystem): ProjectionTestContext[SujetoEvents, SujetoMessageRoots] =
    new SujetoProjectionUnitTestContext
}
