package design_principles.microservice.cassandra_projectionist_microservice

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.ClusterEvent.MemberUp
import akka.http.scaladsl.server.Route
import monitoring.KamonMonitoring

object ProductionMicroserviceContextProvider {

  def getContext(
      ctx: ActorSystem,
      monitoring: KamonMonitoring
  )(visitor: CassandraProjectionistMicroserviceRequirements => Route): Route = {

    visitor(
      CassandraProjectionistMicroserviceRequirements(
        monitoring = monitoring,
        executionContext = ctx.dispatcher,
        ctx = ctx
      )
    )
  }
}
