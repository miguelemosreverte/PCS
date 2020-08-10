package readside.proyectionists.no_registrales.obligacion.infrastructure.main

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
import readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler
import readside.proyectionists.no_registrales.obligacion.ObligacionProjectionHandler

object ObligacionProjectionistMicroservice extends CassandraProjectionistMicroservice {
  override def route(context: CassandraProjectionistMicroserviceRequirements): Route = {
    val monitoring = context.monitoring
    val system = context.ctx.system
    val projectionist = ObligacionProjectionHandler(monitoring, system)
    projectionist.run()
    projectionist.route
  }
}

/*
application
  - handler -- seria como el controller HTTP (estamos haciendo todo en el controller )
dominio
  - persistentSnapshot*/
