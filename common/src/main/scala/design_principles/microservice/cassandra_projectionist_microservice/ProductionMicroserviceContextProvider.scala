package design_principles.microservice.cassandra_projectionist_microservice

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import akka.http.scaladsl.server.Route
import akka.kafka.ConsumerRebalanceEvent
import kafka.{KafkaMessageProcessorRequirements, TopicListener}
import monitoring.KamonMonitoring

object ProductionMicroserviceContextProvider {

  def getContext(
      ctx: ActorContext[MemberUp]
  )(visitor: CassandraProjectionistMicroserviceRequirements => Route): Route = {
    import akka.actor.typed.scaladsl.adapter._

    val monitoring = new KamonMonitoring

    visitor(
      CassandraProjectionistMicroserviceRequirements(
        monitoring = monitoring,
        executionContext = ctx.system.toClassic.dispatcher,
        ctx = ctx
      )
    )
  }
}
