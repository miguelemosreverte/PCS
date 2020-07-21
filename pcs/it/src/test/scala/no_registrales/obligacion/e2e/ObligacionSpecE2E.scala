package no_registrales.obligacion.e2e

import akka.actor.ActorSystem
import consumers.no_registral.obligacion.application.entities.ObligacionMessage
import consumers.no_registral.obligacion.domain.ObligacionEvents
import no_registrales.{obligacion, NoRegistralesTestSuiteE2E}
import org.scalatest.Ignore
import spec.consumers.no_registrales.obligacion.acceptance.ObligacionProjectionAcceptanceTestKit
import spec.testkit.ProjectionTestkit

@Ignore
class ObligacionSpecE2E extends obligacion.ObligacionSpec with NoRegistralesTestSuiteE2E {

  override def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[ObligacionEvents, ObligacionMessage.ObligacionMessageRoots] =
    new ObligacionProjectionAcceptanceTestKit(
      context.asInstanceOf[E2ETestContext].cassandraTestkit
    )
}
