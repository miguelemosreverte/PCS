package registrales.exencion.unit

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ExencionMessageRoot
import consumers.no_registral.objeto.domain.ObjetoEvents
import registrales.exencion.ExencionTestSuiteMock
import spec.consumers.registrales.exencion.unit_test.ExencionProjectionUnitTestKit
import spec.testkit.ProjectionTestkit

class ExencionSpecUT extends ExencionTestSuiteMock {

  override def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[ObjetoEvents.ObjetoAddedExencion, ExencionMessageRoot] =
    new ExencionProjectionUnitTestKit(
      context.asInstanceOf[ExencionMockE2ETestContext].cassandraTestkit
    )
}
