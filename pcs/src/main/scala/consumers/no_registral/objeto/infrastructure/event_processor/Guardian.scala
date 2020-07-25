package consumers.no_registral.objeto.infrastructure.event_processor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardedDaemonProcessSettings}
import akka.projection.ProjectionBehavior
import akka.projections.{ProjectionFactory, ProjectionSettings}
import org.slf4j.LoggerFactory

object Guardian {
  private val log = LoggerFactory.getLogger(this.getClass)
  def apply(): Behavior[Nothing] = {
    Behaviors.setup[Nothing] { context =>
      implicit val system: ActorSystem[Nothing] = context.system

      log.info("Running Proyections")
      val shardingSettings = ClusterShardingSettings(system)
      val shardedDaemonProcessSettings = ShardedDaemonProcessSettings(system).withShardingSettings(shardingSettings)

      val objetoSettings = ProjectionSettings("ObjetoNovedadCotitularidad", 1)
      ShardedDaemonProcess(system).init(
        name = "ObjetoNovedadCotitularidad",
        objetoSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "objeto-novedad-cotitularidad",
              new ObjetoNovedadCotitularidadProjectionHandler(objetoSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      Behaviors.empty
    }
  }
}
