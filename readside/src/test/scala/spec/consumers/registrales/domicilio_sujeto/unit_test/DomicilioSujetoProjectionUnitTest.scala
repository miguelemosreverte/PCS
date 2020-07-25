package spec.consumers.registrales.domicilio_sujeto.unit_test

import akka.actor.ActorSystem
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import spec.consumers.registrales.domicilio_sujeto.DomicilioSujetoProyectionistSpec
import spec.testsuite.ProjectionTestContext

class DomicilioSujetoProjectionUnitTest extends DomicilioSujetoProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[DomicilioSujetoEvents, DomicilioSujetoMessage.DomicilioSujetoMessageRoots] =
    new DomicilioSujetoProjectionUnitTestContext
}
