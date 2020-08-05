package no_registrales.objeto.unit

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.objeto.domain.ObjetoEvents
import no_registrales.objeto.ObjetoSpec
import no_registrales.{BaseE2ESpec, NoRegistralesTestSuiteMock}
import spec.consumers.no_registrales.objeto.unit_test.ObjetoProjectionUnitTestKit
import spec.testkit.ProjectionTestkit

class ObjetoSpecUT extends ObjetoSpec with BaseE2ESpec with NoRegistralesTestSuiteMock {
  override def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[ObjetoEvents, ObjetoMessage.ObjetoMessageRoots] =
    new ObjetoProjectionUnitTestKit(
      context.asInstanceOf[MockE2ETestContext].cassandraTestkit
    )
}
