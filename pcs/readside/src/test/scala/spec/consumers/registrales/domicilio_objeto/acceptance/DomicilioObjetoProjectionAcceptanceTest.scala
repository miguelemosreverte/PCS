package spec.consumers.registrales.domicilio_objeto.acceptance

import akka.actor.ActorSystem
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoMessage
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents
import org.scalatest.Ignore
import spec.consumers.registrales.domicilio_objeto.DomicilioObjetoProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class DomicilioObjetoProjectionAcceptanceTest extends DomicilioObjetoProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[DomicilioObjetoEvents, DomicilioObjetoMessage.DomicilioObjetoMessageRoots] =
    new DomicilioObjetoProjectionAcceptanceTestContext
}
