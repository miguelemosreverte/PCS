package design_principles.actor_model.testkit

import scala.concurrent.Future
import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.kafka.{ConsumerRebalanceEvent, ProducerSettings}
import akka.stream.UniqueKillSwitch
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import kafka.{
  KafkaMessageProcessorRequirements,
  KafkaMessageProducer,
  KafkaTransactionalMessageProcessor,
  TopicListener
}
import monitoring.{DummyMonitoring, Monitoring}
import akka.actor.typed.scaladsl.adapter._

class KafkaTestkit(monitoring: Monitoring)(implicit system: ActorSystem) {

  val rebalancerListener: ActorRef[ConsumerRebalanceEvent] =
    system.spawn(
      TopicListener(
        typeKeyName = "rebalancerListener",
        monitoring
      ),
      name = "rebalancerListener"
    )

  private implicit def kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements =
    KafkaMessageProcessorRequirements.productionSettings(rebalancerListener.toClassic, monitoring, system)
  private implicit def producerSettings: ProducerSettings[String, String] =
    KafkaMessageProcessorRequirements.productionSettings(rebalancerListener.toClassic, monitoring, system).producer
  def messageProducer: KafkaMessageProducer = new KafkaMessageProducer()

  def messageProcessor: KafkaTransactionalMessageProcessor with MessageProcessorLogging =
    new KafkaTransactionalMessageProcessor(kafkaMessageProcessorRequirements) with MessageProcessorLogging {
      override def run(SOURCE_TOPIC: String,
                       SINK_TOPIC: String,
                       algorithm: String => Future[Seq[String]]): (UniqueKillSwitch, Future[Done]) = {
        def loggedAlgorithm: String => Future[Seq[String]] = { message: String =>
          messageHistory = messageHistory :+ ((SOURCE_TOPIC, message))
          algorithm(message)
        }

        super.run(SOURCE_TOPIC, SINK_TOPIC, loggedAlgorithm)
      }
    }
}
