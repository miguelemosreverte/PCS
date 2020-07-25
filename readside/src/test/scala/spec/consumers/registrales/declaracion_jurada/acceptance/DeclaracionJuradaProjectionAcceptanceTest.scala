package spec.consumers.registrales.declaracion_jurada.acceptance

import akka.actor.ActorSystem
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaMessage
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents
import org.scalatest.Ignore
import spec.consumers.registrales.declaracion_jurada.DeclaracionJuradaProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class DeclaracionJuradaProjectionAcceptanceTest extends DeclaracionJuradaProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[DeclaracionJuradaEvents, DeclaracionJuradaMessage.DeclaracionJuradaMessageRoots] =
    new DeclaracionJuradaProjectionAcceptanceTestContext
}
