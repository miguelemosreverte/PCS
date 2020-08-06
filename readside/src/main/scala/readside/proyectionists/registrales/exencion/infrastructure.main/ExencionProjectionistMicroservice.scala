package readside.proyectionists.registrales.exencion.infrastructure.main

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
import readside.proyectionists.registrales.domicilio_sujeto.DomicilioSujetoProjectionHandler
import readside.proyectionists.registrales.exencion.ExencionProjectionHandler

object ExencionProjectionistMicroservice extends CassandraProjectionistMicroservice {
  override def route(context: CassandraProjectionistMicroserviceRequirements): Route = {
    val monitoring = context.monitoring
    val system = context.ctx.system
    ExencionProjectionHandler(monitoring, system).route
  }
}
