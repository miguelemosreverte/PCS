package no_registrales.sujeto.unit

import akka.actor.ActorSystem
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents
import no_registrales.sujeto.SujetoSpec
import no_registrales.{BaseE2ESpec, NoRegistralesTestSuiteMock}
import spec.consumers.no_registrales.sujeto.SujetoProyectionistUnitTest

class SujetoSpecUT extends SujetoSpec with BaseE2ESpec with NoRegistralesTestSuiteMock {

  def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): spec.consumers.ProjectionTestkit[SujetoEvents, SujetoMessageRoots] =
    new SujetoProyectionistUnitTest.SujetoProjectionTestkit(context.asInstanceOf[MockE2ETestContext].cassandraTestkit)
}
