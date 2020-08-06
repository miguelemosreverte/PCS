package readside.proyectionists.registrales.etapas_procesales.infrastructure.main

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
import readside.proyectionists.registrales.etapas_procesales.EtapasProcesalesProjectionHandler

object EtapasProcesalesProjectionistMicroservice extends CassandraProjectionistMicroservice {
  override def route(context: CassandraProjectionistMicroserviceRequirements): Route = {

    val monitoring = context.monitoring
    val system = context.ctx.system
    val shardingSettings = ClusterShardingSettings(system)
    val shardedDaemonProcessSettings = ShardedDaemonProcessSettings(system).withShardingSettings(shardingSettings)
    val domicilioSujetoSettings = ProjectionSettings("EtapasProcesales", 1, monitoring)
    ShardedDaemonProcess(system).init(
      name = "EtapasProcesalesProjection",
      domicilioSujetoSettings.parallelism,
      _ =>
        ProjectionBehavior(
          ProjectionFactory.createProjectionFor(
            system,
            "etapas_procesales",
            new EtapasProcesalesProjectionHandler(domicilioSujetoSettings, system)
          )
        ),
      shardedDaemonProcessSettings,
      Some(ProjectionBehavior.Stop)
    )
    import akka.http.scaladsl.server.Directives._

    path("api" / "projection" / "etapas_procesales" / "ready") {
      complete(StatusCodes.OK)
    }
  }
}
