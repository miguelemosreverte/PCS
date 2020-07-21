package design_principles.actor_model.testkit

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.stream.UniqueKillSwitch
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import kafka.{KafkaMessageProcessorRequirements, KafkaMessageProducer, KafkaTransactionalMessageProcessor}

class KafkaTestkit(implicit system: ActorSystem) {
  private implicit def kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements =
    KafkaMessageProcessorRequirements.productionSettings()
  private implicit def producerSettings: ProducerSettings[String, String] =
    KafkaMessageProcessorRequirements.productionSettings().producer
  def messageProducer: KafkaMessageProducer = new KafkaMessageProducer()

  def messageProcessor: KafkaTransactionalMessageProcessor with MessageProcessorLogging =
    new KafkaTransactionalMessageProcessor() with MessageProcessorLogging {
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
