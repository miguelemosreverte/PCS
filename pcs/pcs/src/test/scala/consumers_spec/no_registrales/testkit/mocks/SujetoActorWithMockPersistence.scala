package consumers_spec.no_registrales.testkit.mocks

import akka.actor.{ActorSystem, Props}
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.ShardedEntityNoRequirements
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import kafka.MessageProducer

class SujetoActorWithMockPersistence(
    messageProducer: MessageProducer
)(implicit system: ActorSystem)
    extends ShardedEntityNoRequirements {

  override def props(requirements: ShardedEntity.NoRequirements): Props = Props(
    new SujetoActor(
      new ObjetoActorWithMockPersistence(messageProducer).props
    )
  )
}
