package consumers_spec.no_registrales.testkit.mocks

import akka.actor.Props
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor

class ObligacionActorWithMockPersistence() {
  def props: Props =
    Props(new ObligacionActor())
}
