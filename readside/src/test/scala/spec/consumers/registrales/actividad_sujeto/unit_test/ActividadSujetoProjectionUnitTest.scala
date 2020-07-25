package spec.consumers.registrales.actividad_sujeto.unit_test

import akka.actor.ActorSystem
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoMessage
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents
import spec.consumers.registrales.actividad_sujeto.ActividadSujetoProyectionistSpec
import spec.testsuite.ProjectionTestContext

class ActividadSujetoProjectionUnitTest extends ActividadSujetoProyectionistSpec {
  override def testContext()(
      implicit system: ActorSystem
  ): ProjectionTestContext[ActividadSujetoEvents, ActividadSujetoMessage.ActividadSujetoMessageRoots] =
    new ActividadSujetoProjectionUnitTestContext
}
