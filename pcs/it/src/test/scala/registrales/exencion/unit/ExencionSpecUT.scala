package registrales.exencion.unit

import akka.actor.ActorSystem
import consumers.no_registral.objeto.domain.ObjetoEvents
import registrales.exencion.ExencionTestSuiteMock
import spec.consumers.ProjectionTestkit
import spec.consumers.registrales.exencion.{ExencionProyectionistSpec, ExencionProyectionistUnitTest}

class ExencionSpecUT extends ExencionTestSuiteMock {

  override def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[ObjetoEvents.ObjetoAddedExencion, ExencionProyectionistSpec.ExencionMessageRoot] =
    new ExencionProyectionistUnitTest.ExencionProjectionTestkit(
      context.asInstanceOf[ExencionMockE2ETestContext].cassandraTestkit
    )
}
