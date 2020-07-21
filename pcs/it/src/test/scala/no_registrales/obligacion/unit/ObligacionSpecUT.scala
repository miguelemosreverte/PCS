package no_registrales.obligacion.unit

import akka.actor.ActorSystem
import consumers.no_registral.obligacion.application.entities.ObligacionMessage
import consumers.no_registral.obligacion.domain.ObligacionEvents
import no_registrales.{BaseE2ESpec, NoRegistralesTestSuiteMock}
import no_registrales.obligacion.ObligacionSpec
import spec.consumers.ProjectionTestkit
import spec.consumers.no_registrales.obligacion.ObligacionProyectionistUnitTest

class ObligacionSpecUT extends ObligacionSpec with BaseE2ESpec with NoRegistralesTestSuiteMock {

  override def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[ObligacionEvents, ObligacionMessage.ObligacionMessageRoots] =
    new ObligacionProyectionistUnitTest.ObligacionProjectionTestkit(
      context.asInstanceOf[MockE2ETestContext].cassandraTestkit
    )
}
