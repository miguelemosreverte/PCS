package kafka

import akka.kafka.{ConsumerSettings, ProducerSettings}
import com.typesafe.config.ConfigFactory
import monitoring.Monitoring
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

case class KafkaMessageProcessorRequirements(system: akka.actor.ActorSystem,
                                             rebalancerListener: akka.actor.ActorRef,
                                             monitoring: Monitoring,
                                             consumer: ConsumerSettings[String, String],
                                             producer: ProducerSettings[String, String])

object KafkaMessageProcessorRequirements {

  private val config = ConfigFactory.load()
  private val appConfig = new KafkaConfig(config)
  val bootstrapServers: String = appConfig.KAFKA_BROKER

  private implicit def consumerSettings(system: akka.actor.ActorSystem): ConsumerSettings[String, String] =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withGroupId(appConfig.CONSUMER_GROUP)
      .withBootstrapServers(bootstrapServers)

  private implicit def producerSettings(system: akka.actor.ActorSystem): ProducerSettings[String, String] =
    ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  def productionSettings(rebalanceListener: akka.actor.ActorRef,
                         monitoring: Monitoring,
                         system: akka.actor.ActorSystem) =
    KafkaMessageProcessorRequirements(
      system,
      rebalanceListener,
      monitoring,
      consumerSettings(system),
      producerSettings(system)
    )
}
