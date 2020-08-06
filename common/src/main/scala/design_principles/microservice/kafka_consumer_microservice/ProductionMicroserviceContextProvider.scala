package design_principles.microservice.kafka_consumer_microservice

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.ClusterEvent.MemberUp
import akka.http.scaladsl.server.Route
import akka.kafka.ConsumerRebalanceEvent
import kafka.{KafkaMessageProcessorRequirements, TopicListener}
import monitoring.KamonMonitoring

object ProductionMicroserviceContextProvider {
  def getContext(ctx: ActorContext[MemberUp])(visitor: KafkaConsumerMicroserviceRequirements => Route): Route = {
    val monitoring = new KamonMonitoring

    val rebalancerListener: ActorRef[ConsumerRebalanceEvent] =
      ctx.spawn(
        TopicListener(
          typeKeyName = "rebalancerListener",
          monitoring
        ),
        name = "rebalancerListener"
      )

    val transactionRequirements: KafkaMessageProcessorRequirements =
      KafkaMessageProcessorRequirements.productionSettings(
        Some(rebalancerListener.toClassic),
        monitoring,
        ctx.system.toClassic
      )

    visitor(
      KafkaConsumerMicroserviceRequirements(
        monitoring = monitoring,
        executionContext = ctx.system.toClassic.dispatcher,
        ctx = ctx,
        kafkaMessageProcessorRequirements = transactionRequirements
      )
    )
  }
}
