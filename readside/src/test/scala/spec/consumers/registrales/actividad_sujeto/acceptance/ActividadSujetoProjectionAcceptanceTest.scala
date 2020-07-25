package spec.consumers.registrales.actividad_sujeto.acceptance

import akka.actor.ActorSystem
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents
import org.scalatest.Ignore
import spec.consumers.registrales.actividad_sujeto.ActividadSujetoProyectionistSpec
import spec.testsuite.ProjectionTestContext

@Ignore
class ActividadSujetoProjectionAcceptanceTest extends ActividadSujetoProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[ActividadSujetoEvents, ActividadSujetoMessage.ActividadSujetoMessageRoots] =
    new ActividadSujetoProjectionAcceptanceTestContext
}
