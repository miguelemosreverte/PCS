package registrales.exencion.testkit.mocks

import akka.actor.{ActorSystem, Props}
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.ShardedEntityNoRequirements
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import config.MockConfig
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import kafka.MessageProducer
import monitoring.Monitoring

class SujetoActorWithMockPersistence(
    exencionProjectionist: Handler[EventEnvelope[ObjetoAddedExencion]]
)(implicit system: ActorSystem)
    extends ShardedEntity[Monitoring] {

  override def props(monitoring: Monitoring): Props = {
    val objetoProps = new ObjetoActorWithMockPersistence(exencionProjectionist).props(monitoring)
    Props(
      new SujetoActor(
        monitoring,
        Some(objetoProps),
        MockConfig.config
      )
    )
  }
}
