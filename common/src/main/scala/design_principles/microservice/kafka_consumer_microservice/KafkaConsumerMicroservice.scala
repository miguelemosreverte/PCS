package design_principles.microservice.kafka_consumer_microservice

import api.actor_transaction.{ActorTransaction, ActorTransactionController}
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.microservice.Microservice
import kafka.{KafkaMessageProcessorRequirements, KafkaMessageProducer}
import monitoring.{KamonMonitoring, Monitoring}
import akka.actor.typed.scaladsl.adapter._
import akka.entity.ShardedEntity.ProductionMonitoringAndMessageProducer

abstract class KafkaConsumerMicroservice(implicit m: KafkaConsumerMicroserviceRequirements)
    extends Microservice[KafkaConsumerMicroserviceRequirements] {

  implicit final val classicSystem: akka.actor.ActorSystem = m.ctx
  implicit final val system: akka.actor.typed.ActorSystem[Nothing] = m.ctx.toTyped

  implicit final val monitoring: KamonMonitoring = m.monitoring
  implicit final val queryStateApiR: QueryStateApiRequirements = m.queryStateApiRequirements
  implicit final val kafkaMessageProcessorR: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
  implicit final val actorTransactionR: ActorTransaction.ActorTransactionRequirements = m.actorTransactionRequirements
  implicit final val monitoringAndMessageProducer: ProductionMonitoringAndMessageProducer =
    ProductionMonitoringAndMessageProducer(
      monitoring,
      KafkaMessageProducer(monitoring, m.kafkaMessageProcessorRequirements.rebalancerListener)
    )
  def actorTransactions: Set[ActorTransaction[_]]

  final def actorTransactionControllers: Set[ActorTransactionController] =
    actorTransactions.map(_.controller)
}
