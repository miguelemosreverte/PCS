package design_principles.external_pub_sub.kafka

import akka.Done
import akka.entity.ShardedEntity
import akka.stream.UniqueKillSwitch
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.{
  KafkaMessageProcessorRequirements,
  KafkaMessageProducer,
  KafkaTransactionalMessageProcessor,
  MessageProcessor,
  MessageProducer
}

import scala.concurrent.Future

class KafkaProduction(implicit actorRequirements: ShardedEntity.ProductionMonitoringAndMessageProducer)
    extends MessageProducer
    with MessageProcessor
    with MessageProcessorLogging {
  val messageProducer: MessageProducer = actorRequirements.messageProducer

  val messageProcessor: KafkaTransactionalMessageProcessor with MessageProcessorLogging with MessageProducer =
    new KafkaTransactionalMessageProcessor(
      KafkaMessageProcessorRequirements.productionSettings(
        actorRequirements.rebalanceListener,
        actorRequirements.monitoring,
        actorRequirements.system,
        actorRequirements.system.dispatcher
      )
    ) with MessageProcessorLogging with MessageProducer {
      override def run(SOURCE_TOPIC: String,
                       SINK_TOPIC: String,
                       algorithm: String => Future[Seq[String]]): (Option[UniqueKillSwitch], Future[Done]) = {
        super.run(SOURCE_TOPIC, SINK_TOPIC, { string =>
          messageHistory = messageHistory :+ ((SOURCE_TOPIC, string))
          algorithm(string)
        })
      }

      override def createTopic(topic: String): Future[Done] =
        messageProducer.createTopic(topic)

      override def produce(data: Seq[KafkaMessageProducer.KafkaKeyValue],
                           topic: String)(handler: Seq[KafkaMessageProducer.KafkaKeyValue] => Unit): Future[Done] =
        messageProducer.produce(data, topic)(handler)

    }

  override type MessageProcessorKillSwitch = messageProcessor.MessageProcessorKillSwitch
  override def run(SOURCE_TOPIC: String,
                   SINK_TOPIC: String,
                   algorithm: String => Future[Seq[String]]): (Option[MessageProcessorKillSwitch], Future[Done]) =
    messageProcessor.run(SOURCE_TOPIC, SINK_TOPIC, algorithm)

  override def createTopic(topic: String): Future[Done] =
    messageProcessor.createTopic(topic)

  override def produce(data: Seq[KafkaKeyValue], topic: String)(handler: Seq[KafkaKeyValue] => Unit): Future[Done] =
    messageProcessor.produce(data, topic)(handler)
}
