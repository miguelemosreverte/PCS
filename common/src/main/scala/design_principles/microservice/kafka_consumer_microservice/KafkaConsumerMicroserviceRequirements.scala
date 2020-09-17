package design_principles.microservice.kafka_consumer_microservice

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.microservice.MicroserviceRequirements
import kafka.KafkaMessageProcessorRequirements
import monitoring.{KamonMonitoring, Monitoring}

import scala.concurrent.ExecutionContext

case class KafkaConsumerMicroserviceRequirements(
    monitoring: KamonMonitoring,
    ctx: ActorSystem,
    queryStateApiRequirements: QueryStateApiRequirements,
    actorTransactionRequirements: ActorTransactionRequirements,
    kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements,
    config: Config
) extends MicroserviceRequirements
