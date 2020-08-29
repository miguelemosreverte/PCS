package no_registrales.mocks

import akka.actor.{ActorSystem, Props}
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.ShardedEntityNoRequirements
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import config.MockConfig
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor.SujetoTags
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite.eventEnvelope
import kafka.MessageProducer
import monitoring.Monitoring

class SujetoActorWithMockPersistence(
    obligacionProyectionist: Handler[EventEnvelope[ObligacionEvents]],
    objetoProyectionist: Handler[EventEnvelope[ObjetoEvents]],
    sujetoProyectionist: Handler[EventEnvelope[SujetoEvents]],
    validateReadside: Any => Any,
    messageProducer: MessageProducer
)(implicit system: ActorSystem)
    extends ShardedEntity[Monitoring] {

  override def props(monitoring: Monitoring): Props = {
    val objetoProps =
      new ObjetoActorWithMockPersistence(obligacionProyectionist,
                                         objetoProyectionist,
                                         validateReadside,
                                         messageProducer).props(monitoring)
    Props(
      new SujetoActor(monitoring, Some(objetoProps), MockConfig.config) {
        override def persistEvent(event: SujetoEvents, tags: Set[String])(handler: () => Unit): Unit = {
          super.persistEvent(event, tags)(handler)
          if (SujetoTags.SujetoReadside subsetOf tags) {
            sujetoProyectionist.process(eventEnvelope(event))
            validateReadside(event)
          }
        }
      }
    )
  }
}
