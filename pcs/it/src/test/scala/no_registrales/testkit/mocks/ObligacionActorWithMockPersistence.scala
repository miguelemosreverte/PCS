package no_registrales.testkit.mocks

import akka.actor.Props
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor.ObligacionTags
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite.eventEnvelope

class ObligacionActorWithMockPersistence(
    validateReadside: Any => Any,
    obligacionProyectionist: Handler[EventEnvelope[ObligacionEvents]]
) {
  def props: Props =
    Props(new ObligacionActor() {
      override def persistEvent(event: ObligacionEvents, tags: Set[String])(handler: () => Unit): Unit = {
        super.persistEvent(event, tags)(handler)
        if (ObligacionTags.ObligacionReadside subsetOf tags) {
          obligacionProyectionist.process(eventEnvelope(event))
          validateReadside(event)
        }
      }
    })
}
