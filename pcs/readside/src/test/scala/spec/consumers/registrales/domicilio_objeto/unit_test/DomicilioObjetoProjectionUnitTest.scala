package spec.consumers.registrales.domicilio_objeto.unit_test

import akka.actor.ActorSystem
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoMessage
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents
import spec.consumers.registrales.domicilio_objeto.DomicilioObjetoProyectionistSpec
import spec.testsuite.ProjectionTestContext

class DomicilioObjetoProjectionUnitTest extends DomicilioObjetoProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[DomicilioObjetoEvents, DomicilioObjetoMessage.DomicilioObjetoMessageRoots] =
    new DomicilioObjetoProjectionUnitTestContext
}
