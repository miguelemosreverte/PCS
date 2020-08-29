package consumers_spec.no_registrales.testkit.mocks

import akka.actor.{ActorSystem, Props}
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.ShardedEntityNoRequirements
import config.MockConfig
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import kafka.MessageProducer
import monitoring.{DummyMonitoring, Monitoring}

class SujetoActorWithMockPersistence(
    messageProducer: MessageProducer,
    monitoring: Monitoring = new DummyMonitoring
)(implicit system: ActorSystem)
    extends ShardedEntityNoRequirements {

  override def props(requirements: ShardedEntity.NoRequirements): Props = {
    val objetoProps = new ObjetoActorWithMockPersistence(messageProducer).props(monitoring)
    Props(
      new SujetoActor(
        monitoring,
        Some(objetoProps),
        MockConfig.config
      )
    )
  }
}
