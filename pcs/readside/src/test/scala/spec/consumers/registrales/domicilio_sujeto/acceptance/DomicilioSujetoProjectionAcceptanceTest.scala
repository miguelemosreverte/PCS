package spec.consumers.registrales.domicilio_sujeto.acceptance

import akka.actor.ActorSystem
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import org.scalatest.Ignore
import spec.consumers.registrales.domicilio_sujeto.DomicilioSujetoProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class DomicilioSujetoProjectionAcceptanceTest extends DomicilioSujetoProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[DomicilioSujetoEvents, DomicilioSujetoMessage.DomicilioSujetoMessageRoots] =
    new DomicilioSujetoProjectionAcceptanceTestContext
}
