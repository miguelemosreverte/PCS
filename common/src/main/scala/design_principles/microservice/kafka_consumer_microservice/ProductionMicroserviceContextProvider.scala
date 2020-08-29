package design_principles.microservice.kafka_consumer_microservice

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.ClusterEvent.MemberUp
import akka.entity.ShardedEntity.ShardedEntityRequirements
import akka.http.scaladsl.server.Route
import akka.kafka.ConsumerRebalanceEvent
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.Config
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import kafka.{KafkaMessageProcessorRequirements, TopicListener}
import monitoring.KamonMonitoring

object ProductionMicroserviceContextProvider {
  def getContext(ctx: ActorSystem, config: Config)(visitor: KafkaConsumerMicroserviceRequirements => Route): Route = {

    implicit val monitoring: KamonMonitoring = new KamonMonitoring

    val rebalancerListener: ActorRef[ConsumerRebalanceEvent] =
      ctx.spawn(
        TopicListener(
          typeKeyName = "rebalancerListener",
          monitoring
        ),
        name = "rebalancerListener"
      )

    val kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements =
      KafkaMessageProcessorRequirements.productionSettings(
        rebalancerListener.toClassic,
        monitoring,
        system = ctx,
        executionContext = ctx.dispatcher
      )
    val queryStateApiRequirements = QueryStateApiRequirements(
      system = ctx,
      executionContext = ctx.dispatcher
    )
    val shardedEntityRequirements = ShardedEntityRequirements(
      system = ctx
    )
    val actorTransactionRequirements = ActorTransactionRequirements(
      executionContext = ctx.dispatcher,
      config = config
    )

    visitor(
      KafkaConsumerMicroserviceRequirements(
        monitoring = monitoring,
        ctx = ctx,
        queryStateApiRequirements,
        shardedEntityRequirements,
        actorTransactionRequirements,
        kafkaMessageProcessorRequirements,
        config
      )
    )
  }
}
