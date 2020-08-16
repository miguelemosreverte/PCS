package design_principles.microservice.kafka_consumer_microservice

import akka.actor.{ActorSystem, Props}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.ClusterEvent.MemberUp
import akka.http.scaladsl.server.Route
import akka.kafka.ConsumerRebalanceEvent
import kafka.{KafkaMessageProcessorRequirements, TopicListener}
import monitoring.KamonMonitoring

object ProductionMicroserviceContextProvider {
  def getContext(ctx: ActorSystem)(visitor: KafkaConsumerMicroserviceRequirements => Route): Route = {
    val monitoring = new KamonMonitoring

    val rebalancerListener =
      ctx.actorOf(Props(
                    new TopicListener(
                      typeKeyName = "rebalancerListener",
                      monitoring
                    )
                  ),
                  name = "rebalancerListener")

    val transactionRequirements: KafkaMessageProcessorRequirements =
      KafkaMessageProcessorRequirements.productionSettings(
        rebalancerListener,
        monitoring,
        ctx
      )

    visitor(
      KafkaConsumerMicroserviceRequirements(
        monitoring = monitoring,
        executionContext = ctx.dispatcher,
        ctx = ctx,
        kafkaMessageProcessorRequirements = transactionRequirements
      )
    )
  }
}
