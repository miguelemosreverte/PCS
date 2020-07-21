package no_registrales.sujeto.e2e

import akka.actor.ActorSystem
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import no_registrales.NoRegistralesTestSuiteE2E
import no_registrales.sujeto.SujetoSpec
import org.scalatest.Ignore
import spec.consumers.no_registrales.sujeto.acceptance.SujetoProjectionAcceptanceTestKit
import spec.testkit.ProjectionTestkit

@Ignore
class SujetoSpecE2E extends SujetoSpec with NoRegistralesTestSuiteE2E {

  def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[SujetoEvents, SujetoMessageRoots] =
    new SujetoProjectionAcceptanceTestKit(context.asInstanceOf[E2ETestContext].cassandraTestkit)
}
