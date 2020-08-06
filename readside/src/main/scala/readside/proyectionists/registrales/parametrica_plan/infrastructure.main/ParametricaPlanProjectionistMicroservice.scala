package readside.proyectionists.registrales.parametrica_plan.infrastructure.main

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
import readside.proyectionists.registrales.parametrica_plan.ParametricaPlanProjectionHandler

object ParametricaPlanProjectionistMicroservice extends CassandraProjectionistMicroservice {
  override def route(context: CassandraProjectionistMicroserviceRequirements): Route = {

    val monitoring = context.monitoring
    val system = context.ctx.system
    val shardingSettings = ClusterShardingSettings(system)
    val shardedDaemonProcessSettings = ShardedDaemonProcessSettings(system).withShardingSettings(shardingSettings)
    val domicilioSujetoSettings = ProjectionSettings("ParametricaPlan", 1, monitoring)
    ShardedDaemonProcess(system).init(
      name = "ParametricaPlanProjection",
      domicilioSujetoSettings.parallelism,
      _ =>
        ProjectionBehavior(
          ProjectionFactory.createProjectionFor(
            system,
            "parametrica_plan",
            new ParametricaPlanProjectionHandler(domicilioSujetoSettings, system)
          )
        ),
      shardedDaemonProcessSettings,
      Some(ProjectionBehavior.Stop)
    )
    import akka.http.scaladsl.server.Directives._

    path("api" / "projection" / "parametrica_plan" / "ready") {
      complete(StatusCodes.OK)
    }
  }
}
