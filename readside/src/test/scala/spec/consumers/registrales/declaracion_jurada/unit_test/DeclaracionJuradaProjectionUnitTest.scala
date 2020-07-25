package spec.consumers.registrales.declaracion_jurada.unit_test

import akka.actor.ActorSystem
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaMessage
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents
import spec.consumers.registrales.declaracion_jurada.DeclaracionJuradaProyectionistSpec
import spec.testsuite.ProjectionTestContext

class DeclaracionJuradaProjectionUnitTest extends DeclaracionJuradaProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[DeclaracionJuradaEvents, DeclaracionJuradaMessage.DeclaracionJuradaMessageRoots] =
    new DeclaracionJuradaProjectionUnitTestContext
}
