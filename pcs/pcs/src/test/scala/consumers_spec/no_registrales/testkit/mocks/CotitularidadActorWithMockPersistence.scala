package consumers_spec.no_registrales.testkit.mocks

import scala.concurrent.Future

import akka.Done
import akka.actor.{ActorSystem, Props}
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.ShardedEntityNoRequirements
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import kafka.{KafkaMessageProcessorRequirements, MessageProducer}

class CotitularidadActorWithMockPersistence(messageProducer: MessageProducer)(implicit system: ActorSystem)
    extends ShardedEntityNoRequirements {
  override def props(requirements: ShardedEntity.NoRequirements): Props = Props(
    new CotitularidadActor(KafkaMessageProcessorRequirements.productionSettings()) {

      override def publishToKafka(messages: Seq[String], topic: String): (Seq[String] => Unit) => Future[Done] = {
        messageProducer.produce(messages, topic)(_ => ())
        _ => Future.successful(Done)
      }
    }
  )
}
