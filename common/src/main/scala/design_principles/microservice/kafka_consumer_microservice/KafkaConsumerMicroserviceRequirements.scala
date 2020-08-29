package design_principles.microservice.kafka_consumer_microservice

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import akka.entity.ShardedEntity.ShardedEntityRequirements
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.microservice.MicroserviceRequirements
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

case class KafkaConsumerMicroserviceRequirements(
    monitoring: Monitoring,
    ctx: ActorSystem,
    queryStateApiRequirements: QueryStateApiRequirements,
    shardedEntityRequirements: ShardedEntityRequirements,
    actorTransactionRequirements: ActorTransactionRequirements,
    kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements,
    config: Config
) extends MicroserviceRequirements
