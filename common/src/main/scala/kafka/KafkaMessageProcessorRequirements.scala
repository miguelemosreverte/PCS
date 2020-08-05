package kafka

import akka.kafka.{ConsumerSettings, ProducerSettings}
import com.typesafe.config.ConfigFactory
import monitoring.Monitoring
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

case class KafkaMessageProcessorRequirements(system: akka.actor.ActorSystem,
                                             rebalancerListener: Option[akka.actor.ActorRef],
                                             monitoring: Monitoring,
                                             consumer: ConsumerSettings[String, String],
                                             producer: ProducerSettings[String, String])

object KafkaMessageProcessorRequirements {

  private val config = ConfigFactory.load()
  private val appConfig = new KafkaConfig(config)
  private val bootstrapServers = appConfig.KAFKA_BROKER

  private implicit def consumerSettings(implicit system: akka.actor.ActorSystem): ConsumerSettings[String, String] =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withGroupId(appConfig.CONSUMER_GROUP)
      .withBootstrapServers(bootstrapServers)

  private implicit def producerSettings(implicit system: akka.actor.ActorSystem): ProducerSettings[String, String] =
    ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  def productionSettings(rebalanceListener: Option[akka.actor.ActorRef],
                         monitoring: Monitoring)(implicit system: akka.actor.ActorSystem) =
    KafkaMessageProcessorRequirements(
      system,
      rebalanceListener,
      monitoring,
      consumerSettings,
      producerSettings
    )
}
