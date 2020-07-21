package kafka

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

case class KafkaMessageProcessorRequirements(system: ActorSystem,
                                             consumer: ConsumerSettings[String, String],
                                             producer: ProducerSettings[String, String])

object KafkaMessageProcessorRequirements {

  private val config = ConfigFactory.load()
  private val appConfig = new KafkaConfig(config)
  private val bootstrapServers = appConfig.KAFKA_BROKER

  private implicit def consumerSettings(implicit system: ActorSystem): ConsumerSettings[String, String] =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withGroupId(appConfig.CONSUMER_GROUP)
      .withBootstrapServers(bootstrapServers)

  private implicit def producerSettings(implicit system: ActorSystem): ProducerSettings[String, String] =
    ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  def productionSettings()(implicit system: ActorSystem) = KafkaMessageProcessorRequirements(
    system,
    consumerSettings,
    producerSettings
  )
}
