package no_registrales.mocks

import akka.actor.Props
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import config.MockConfig
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor.ObligacionTags
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite.eventEnvelope
import monitoring.Monitoring

class ObligacionActorWithMockPersistence(
    validateReadside: Any => Any,
    obligacionProyectionist: Handler[EventEnvelope[ObligacionEvents]]
) {
  def props(monitoring: Monitoring): Props =
    Props(new ObligacionActor(monitoring, MockConfig.config) {
      override def persistEvent(event: ObligacionEvents, tags: Set[String])(handler: () => Unit): Unit = {
        super.persistEvent(event, tags)(handler)
        if (ObligacionTags.ObligacionReadside subsetOf tags) {
          obligacionProyectionist.process(eventEnvelope(event))
          validateReadside(event)
        }
      }
    })
}
