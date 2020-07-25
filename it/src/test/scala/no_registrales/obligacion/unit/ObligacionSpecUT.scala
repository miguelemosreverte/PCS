package no_registrales.obligacion.unit

import akka.actor.ActorSystem
import consumers.no_registral.obligacion.application.entities.ObligacionMessage
import consumers.no_registral.obligacion.domain.ObligacionEvents
import no_registrales.obligacion.ObligacionSpec
import no_registrales.{BaseE2ESpec, NoRegistralesTestSuiteMock}
import spec.consumers.no_registrales.obligacion.unit_test.ObligacionProjectionUnitTestKit
import spec.testkit.ProjectionTestkit

class ObligacionSpecUT extends ObligacionSpec with BaseE2ESpec with NoRegistralesTestSuiteMock {

  override def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[ObligacionEvents, ObligacionMessage.ObligacionMessageRoots] =
    new ObligacionProjectionUnitTestKit(
      context.asInstanceOf[MockE2ETestContext].cassandraTestkit
    )
}
