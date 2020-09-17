package design_principles.microservice.cassandra_projectionist_microservice

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.ClusterEvent.MemberUp
import akka.entity.ShardedEntity.ProductionMonitoringAndCassandraWrite
import akka.http.scaladsl.server.Route
import akka.kafka.ConsumerRebalanceEvent
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.write.CassandraWriteProduction
import com.typesafe.config.Config
import kafka.{KafkaMessageProcessorRequirements, TopicListener}
import monitoring.KamonMonitoring

object ProductionMicroserviceContextProvider {

  def getContext(
      ctx: ActorSystem,
      config: Config
  )(visitor: CassandraProjectionistMicroserviceRequirements => Route): Route = {

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

    val actorTransactionRequirements = ActorTransactionRequirements(
      executionContext = ctx.dispatcher,
      config = config
    )

    val monitoringAndCassandraWrite = new ProductionMonitoringAndCassandraWrite(
      monitoring,
      cassandraWrite = new CassandraWriteProduction,
      actorTransactionRequirements = actorTransactionRequirements
    )
    visitor(
      CassandraProjectionistMicroserviceRequirements(
        monitoring = monitoring,
        executionContext = ctx.dispatcher,
        ctx = ctx,
        kafkaMessageProcessorRequirements = kafkaMessageProcessorRequirements,
        monitoringAndCassandraWrite = monitoringAndCassandraWrite
      )
    )
  }
}
