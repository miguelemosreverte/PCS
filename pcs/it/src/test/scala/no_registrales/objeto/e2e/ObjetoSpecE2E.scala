package no_registrales.objeto.e2e

import akka.actor.ActorSystem
import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.objeto.domain.ObjetoEvents
import no_registrales.objeto.ObjetoSpec
import no_registrales.{BaseE2ESpec, NoRegistralesTestSuiteE2E}
import org.scalatest.Ignore
import spec.consumers.ProjectionTestkit
import spec.consumers.no_registrales.objeto.ObjetoProyectionistAcceptance

@Ignore
class ObjetoSpecE2E extends ObjetoSpec with BaseE2ESpec with NoRegistralesTestSuiteE2E {
  override def ProjectionTestkit(context: TestContext)(
      implicit system: ActorSystem
  ): ProjectionTestkit[ObjetoEvents, ObjetoMessage.ObjetoMessageRoots] =
    new ObjetoProyectionistAcceptance.ObjetoProjectionTestkit(context.asInstanceOf[E2ETestContext].cassandraTestkit)
}
