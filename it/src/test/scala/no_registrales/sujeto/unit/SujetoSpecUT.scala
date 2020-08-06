package no_registrales.sujeto.unit

import akka.actor.ActorSystem
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import no_registrales.sujeto.SujetoSpec
import no_registrales.{BaseE2ESpec, NoRegistralesTestSuiteMock}
import spec.consumers.no_registrales.sujeto.unit_test.SujetoProjectionUnitTestKit
import spec.testkit.ProjectionTestkit

class SujetoSpecUT extends SujetoSpec with BaseE2ESpec with NoRegistralesTestSuiteMock {

  def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[SujetoEvents, SujetoMessageRoots] =
    new SujetoProjectionUnitTestKit(context.asInstanceOf[MockE2ETestContext].cassandraTestkit)
}
