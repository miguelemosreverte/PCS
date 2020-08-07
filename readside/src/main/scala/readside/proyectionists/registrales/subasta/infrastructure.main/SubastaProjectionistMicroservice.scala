package readside.proyectionists.registrales.subasta.infrastructure.main

import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardedDaemonProcessSettings}
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.projection.ProjectionBehavior
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionFactory
import design_principles.microservice.cassandra_projectionist_microservice.{
  CassandraProjectionistMicroservice,
  CassandraProjectionistMicroserviceRequirements
}
import readside.proyectionists.registrales.plan_pago.PlanPagoProjectionHandler
import readside.proyectionists.registrales.subasta.SubastaProjectionHandler

object SubastaProjectionistMicroservice extends CassandraProjectionistMicroservice {
  override def route(context: CassandraProjectionistMicroserviceRequirements): Route = {
    val monitoring = context.monitoring
    val system = context.ctx.system
    val projectionist = SubastaProjectionHandler(monitoring, system)
    projectionist.run()
    projectionist.route
  }
}
