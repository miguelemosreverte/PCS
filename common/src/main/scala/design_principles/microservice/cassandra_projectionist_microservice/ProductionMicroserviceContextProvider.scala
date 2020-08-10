package design_principles.microservice.cassandra_projectionist_microservice

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.ClusterEvent.MemberUp
import akka.http.scaladsl.server.Route
import monitoring.KamonMonitoring

object ProductionMicroserviceContextProvider {

  def getContext(
      ctx: ActorSystem
  )(visitor: CassandraProjectionistMicroserviceRequirements => Route): Route = {
    val monitoring = new KamonMonitoring

    visitor(
      CassandraProjectionistMicroserviceRequirements(
        monitoring = monitoring,
        executionContext = ctx.dispatcher,
        ctx = ctx
      )
    )
  }
}
