package readside.proyectionists.registrales.exencion.infrastructure.main

import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardedDaemonProcessSettings}
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.projection.ProjectionBehavior
import akka.projections.{ProjectionFactory, ProjectionSettings}
import design_principles.microservice.cassandra_projectionist_microservice.{
  CassandraProjectionistMicroservice,
  CassandraProjectionistMicroserviceRequirements
}
import readside.proyectionists.registrales.exencion.ExencionProjectionHandler

object ExencionProjectionistMicroservice extends CassandraProjectionistMicroservice {
  override def route(context: CassandraProjectionistMicroserviceRequirements): Route = {

    val monitoring = context.monitoring
    val system = context.ctx.system
    val shardingSettings = ClusterShardingSettings(system)
    val shardedDaemonProcessSettings = ShardedDaemonProcessSettings(system).withShardingSettings(shardingSettings)
    val domicilioSujetoSettings = ProjectionSettings("Exencion", 1, monitoring)
    ShardedDaemonProcess(system).init(
      name = "ExencionProjection",
      domicilioSujetoSettings.parallelism,
      _ =>
        ProjectionBehavior(
          ProjectionFactory.createProjectionFor(
            system,
            "exencion",
            new ExencionProjectionHandler(domicilioSujetoSettings, system)
          )
        ),
      shardedDaemonProcessSettings,
      Some(ProjectionBehavior.Stop)
    )
    import akka.http.scaladsl.server.Directives._

    path("api" / "projection" / "exencion" / "ready") {
      complete(StatusCodes.OK)
    }
  }
}