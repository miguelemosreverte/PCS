package readside.proyectionists.registrales.declaracion_jurada.infrastructure.main

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
import readside.proyectionists.registrales.declaracion_jurada.DeclaracionJuradaProjectionHandler

object DeclaracionJuradaProjectionistMicroservice extends CassandraProjectionistMicroservice {
  override def route(context: CassandraProjectionistMicroserviceRequirements): Route = {

    val monitoring = context.monitoring
    val system = context.ctx.system
    val shardingSettings = ClusterShardingSettings(system)
    val shardedDaemonProcessSettings = ShardedDaemonProcessSettings(system).withShardingSettings(shardingSettings)
    val declaracionJuradaSettings = ProjectionSettings("DeclaracionJurada", 1, monitoring)
    ShardedDaemonProcess(system).init(
      name = "DeclaracionJuradaProjection",
      declaracionJuradaSettings.parallelism,
      _ =>
        ProjectionBehavior(
          ProjectionFactory.createProjectionFor(
            system,
            "declaracion_jurada",
            new DeclaracionJuradaProjectionHandler(declaracionJuradaSettings, system)
          )
        ),
      shardedDaemonProcessSettings,
      Some(ProjectionBehavior.Stop)
    )
    import akka.http.scaladsl.server.Directives._

    path("api" / "projection" / "declaracion_jurada" / "ready") {
      complete(StatusCodes.OK)
    }
  }
}
