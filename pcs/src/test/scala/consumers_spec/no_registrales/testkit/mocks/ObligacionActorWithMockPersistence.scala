package consumers_spec.no_registrales.testkit.mocks

import akka.actor.Props
import config.MockConfig
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import monitoring.{DummyMonitoring, Monitoring}

class ObligacionActorWithMockPersistence() {

  def props(monitoring: Monitoring): Props =
    Props(new ObligacionActor(monitoring, MockConfig.config))
}
