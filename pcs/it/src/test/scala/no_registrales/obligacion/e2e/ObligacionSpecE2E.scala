package no_registrales.obligacion.e2e

import akka.actor.ActorSystem
import consumers.no_registral.obligacion.application.entities.ObligacionMessage
import consumers.no_registral.obligacion.domain.ObligacionEvents
import no_registrales.{obligacion, NoRegistralesTestSuiteE2E}
import org.scalatest.Ignore
import spec.consumers.ProjectionTestkit
import spec.consumers.no_registrales.obligacion.ObligacionProyectionistAcceptance

@Ignore
class ObligacionSpecE2E extends obligacion.ObligacionSpec with NoRegistralesTestSuiteE2E {

  override def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[ObligacionEvents, ObligacionMessage.ObligacionMessageRoots] =
    new ObligacionProyectionistAcceptance.ObligacionProjectionTestkit(
      context.asInstanceOf[E2ETestContext].cassandraTestkit
    )
}
