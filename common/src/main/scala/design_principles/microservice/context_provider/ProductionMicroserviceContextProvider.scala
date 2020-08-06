package design_principles.microservice.context_provider

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import akka.http.scaladsl.server.Route
import akka.kafka.ConsumerRebalanceEvent
import design_principles.microservice.MicroserviceRequirements
import kafka.{KafkaMessageProcessorRequirements, TopicListener}
import monitoring.KamonMonitoring

object ProductionMicroserviceContextProvider extends MicroserviceContextProvider {

  override def getContext(ctx: ActorContext[MemberUp])(visitor: MicroserviceRequirements => Route): Route = {
    import akka.actor.typed.scaladsl.adapter._

    val monitoring = new KamonMonitoring

    val rebalancerListener: ActorRef[ConsumerRebalanceEvent] =
      ctx.spawn(
        TopicListener(
          typeKeyName = "rebalancerListener"
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
      MicroserviceRequirements(
        monitoring = monitoring,
        executionContext = ctx.system.toClassic.dispatcher,
        ctx = ctx,
        kafkaMessageProcessorRequirements = transactionRequirements
      )
    )
  }
}
