package consumers_spec.no_registrales.testkit.mocks

import akka.Done
import akka.actor.{ActorSystem, Props}
import akka.entity.ShardedEntity
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import kafka.{KafkaMessageProcessorRequirements, MessageProducer}
import monitoring.Monitoring

import scala.concurrent.Future

class CotitularidadActorWithMockPersistence(messageProducer: MessageProducer)(implicit system: ActorSystem)
    extends ShardedEntity[Monitoring] {
  override def props(monitoring: Monitoring): Props = Props(
    new CotitularidadActor(KafkaMessageProcessorRequirements.productionSettings(None, monitoring, system)) {

      override def publishToKafka(messages: Seq[String], topic: String): (Seq[String] => Unit) => Future[Done] = {
        messageProducer.produce(messages, topic)(_ => ())
        _ => Future.successful(Done)
      }
    }
  )
}
